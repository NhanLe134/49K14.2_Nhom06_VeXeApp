package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.api.CustomerResponse;
import com.example.nhom7vexeapp.models.KhachHang;
import com.example.nhom7vexeapp.viewmodels.CustomerViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProfileActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvDob, btnDeleteAccount;
    private ImageView btnBack, imgAvatar;
    private View btnEditProfileImage; // Dùng View để tương thích với cả ImageView và CardView
    private MaterialButton btnLogout, btnEditInfo;
    private LinearLayout navHome, navSearch, navTickets, navFeedback;

    private CustomerViewModel viewModel;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private String customerUid;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        apiService = ApiClient.getClient().create(ApiService.class);
        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        initViews();
        setupObservers();
        setupPickMedia();

        loadInitialData();
        setupEvents();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvProfileName);
        tvPhone = findViewById(R.id.tvProfilePhone);
        tvDob = findViewById(R.id.tvProfileDob);
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgProfileAvatar);
        btnEditProfileImage = findViewById(R.id.btnEditProfileImage);

        btnEditInfo = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        navHome = findViewById(R.id.nav_home_profile);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets);
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void setupPickMedia() {
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null && imgAvatar != null) {
                Glide.with(this).load(uri).circleCrop().into(imgAvatar);
                getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                        .putString("localAvatarUri", uri.toString()).apply();
                Toast.makeText(this, "Đã cập nhật ảnh đại diện cục bộ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInitialData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        customerUid = pref.getString("customerUid", "");
        if (customerUid.isEmpty()) customerUid = pref.getString("user_id", "");

        if (customerUid.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            handleLogout();
            return;
        }

        // Hiển thị dữ liệu tạm thời từ SharedPreferences
        tvName.setText(pref.getString("customerName", "Khách hàng"));
        tvPhone.setText(pref.getString("customerPhone", ""));
        tvDob.setText(pref.getString("customerDob", "Chưa cập nhật"));

        String localUri = pref.getString("localAvatarUri", "");
        if (!localUri.isEmpty() && imgAvatar != null) {
            Glide.with(this).load(Uri.parse(localUri)).circleCrop().placeholder(R.drawable.nhaxe_home).into(imgAvatar);
        }

        // Gọi API cập nhật dữ liệu mới nhất
        loadAllDataFromServer(customerUid);
    }

    private void loadAllDataFromServer(String uid) {
        // Cách 1: Load Map Detail (Từ File 2 - linh hoạt cho động)
        apiService.getKhachHangDetail(uid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();

                    String name = findValue(data, "Hovaten", "TenKhachHang", "HoTen");
                    if (!name.isEmpty()) tvName.setText(name);

                    String dob = findValue(data, "Ngaysinh", "NgaySinh");
                    if (!dob.isEmpty()) {
                        // Chuẩn hóa định dạng ngày nếu cần
                        if (dob.contains("-")) {
                            try {
                                String[] parts = dob.split("T")[0].split("-");
                                if (parts.length == 3) dob = parts[2] + "/" + parts[1] + "/" + parts[0];
                            } catch (Exception e) {}
                        }
                        tvDob.setText(dob);
                    }

                    String imgData = findValue(data, "AnhDaiDien", "AnhDaiDienURL", "Avatar");
                    if (!imgData.isEmpty() && imgAvatar != null) {
                        Glide.with(CustomerProfileActivity.this).load(imgData).circleCrop().into(imgAvatar);
                    }
                }
                // Luôn gọi thêm Auth Detail để lấy SĐT (thường ở bảng khác)
                fetchPhoneFromAuth(uid);
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                fetchPhoneFromAuth(uid);
            }
        });
    }

    private void fetchPhoneFromAuth(String uid) {
        apiService.getUserAuthDetail(uid).enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String phone = response.body().getSdt();
                    if (phone != null && !phone.isEmpty()) tvPhone.setText(phone);
                }
            }
            @Override public void onFailure(Call<CustomerResponse> call, Throwable t) {}
        });
    }

    private void setupObservers() {
        viewModel.customerData.observe(this, khachHang -> {
            if (khachHang != null) {
                if (khachHang.getHoTen() != null) tvName.setText(khachHang.getHoTen());
                if (khachHang.getNgaySinh() != null) tvDob.setText(khachHang.getNgaySinh());
            }
        });
        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupEvents() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (navHome != null) navHome.setOnClickListener(v -> finish());

        // Hợp nhất logic đổi ảnh: Click ảnh để chọn ảnh, Click bút để vào trang Edit
        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        }

        if (btnEditProfileImage != null || btnEditInfo != null) {
            View.OnClickListener toEdit = v -> {
                Intent intent = new Intent(this, EditCustomerProfileActivity.class);
                startActivityForResult(intent, 300);
            };
            if (btnEditProfileImage != null) btnEditProfileImage.setOnClickListener(toEdit);
            if (btnEditInfo != null) btnEditInfo.setOnClickListener(toEdit);
        }

        if (btnLogout != null) btnLogout.setOnClickListener(v -> handleLogout());

        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xóa tài khoản")
                        .setMessage("Bạn có chắc chắn muốn xóa toàn bộ dữ liệu vĩnh viễn?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteAccount())
                        .setNegativeButton("Hủy", null).show();
            });
        }
    }

    private void deleteAccount() {
        if (customerUid.isEmpty()) return;
        apiService.deleteKhachHang(customerUid).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(CustomerProfileActivity.this, "Đã xóa tài khoản", Toast.LENGTH_SHORT).show();
                handleLogout();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CustomerProfileActivity.this, "Lỗi kết nối khi xóa!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String findValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                String val = map.get(key).toString();
                if (!val.equalsIgnoreCase("null") && !val.isEmpty()) return val;
            }
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK) {
            loadAllDataFromServer(customerUid);
        }
    }
}