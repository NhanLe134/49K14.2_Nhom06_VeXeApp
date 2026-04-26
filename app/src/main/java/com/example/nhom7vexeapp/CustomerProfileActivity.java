package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.nhom7vexeapp.models.KhachHang;
import com.example.nhom7vexeapp.viewmodels.CustomerViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProfileActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvDob;
    private ImageView btnBack, imgAvatar, btnEditProfileImage;
    private MaterialButton btnLogout, btnEditInfo;
    private TextView btnDeleteAccount;
    private LinearLayout navHome, navSearch, navTickets, navFeedback;
    private CustomerViewModel viewModel;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        initViews();
        setupObservers();
        setupEvents();

        // Cấu hình chọn ảnh từ thiết bị
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null && imgAvatar != null) {
                Glide.with(this).load(uri).into(imgAvatar);
                getSharedPreferences("UserPrefs", MODE_PRIVATE).edit()
                        .putString("localAvatarUri", uri.toString()).apply();
                Toast.makeText(this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
            }
        });

        loadData();
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

    private void loadData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        
        String localUri = pref.getString("localAvatarUri", "");
        if (!localUri.isEmpty() && imgAvatar != null) {
            Glide.with(this).load(Uri.parse(localUri)).placeholder(R.drawable.logo).into(imgAvatar);
        }

        String customerUid = pref.getString("customerUid", "");

        if (!customerUid.isEmpty()) {
            loadFromDatabase(customerUid);
        } else {
            loadLocalData();
        }
    }

    private void loadFromDatabase(String uid) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // Sử dụng getProfile để nhận về đối tượng KhachHang chuẩn
        apiService.getProfile(uid).enqueue(new Callback<KhachHang>() {
            @Override
            public void onResponse(Call<KhachHang> call, Response<KhachHang> response) {
                if (response.isSuccessful() && response.body() != null) {
                    KhachHang customer = response.body();
                    tvName.setText(customer.getHoTen() != null ? customer.getHoTen() : "Chưa cập nhật");
                    tvDob.setText(customer.getNgaySinh() != null ? customer.getNgaySinh() : "Chưa cập nhật");

                    // Cập nhật SĐT từ SharedPreferences nếu trên DB chưa có
                    SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    tvPhone.setText(pref.getString("customerPhone", ""));
                }
            }
            @Override public void onFailure(Call<KhachHang> call, Throwable t) {
                Log.e("API_ERROR", "Load profile failed: " + t.getMessage());
                loadLocalData();
            }
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

        View.OnClickListener pickImgClick = v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        
        if (imgAvatar != null) imgAvatar.setOnClickListener(pickImgClick);
        if (btnEditProfileImage != null) btnEditProfileImage.setOnClickListener(pickImgClick);

        if (btnEditInfo != null) {
            btnEditInfo.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditCustomerProfileActivity.class);
                startActivityForResult(intent, 300);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
        }

        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xóa tài khoản")
                        .setMessage("Bạn có chắc chắn muốn xóa toàn bộ dữ liệu vĩnh viễn?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteAccount())
                        .setNegativeButton("Hủy", null).show();
            });
        }

        if (navHome != null) navHome.setOnClickListener(v -> finish());
    }

    private void deleteAccount() {
        String uid = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("customerUid", "");
        if (uid.isEmpty()) return;

        ApiClient.getClient().create(ApiService.class).deleteKhachHang(uid).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(CustomerProfileActivity.this, "Đã xóa tài khoản", Toast.LENGTH_SHORT).show();
                handleLogout();
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void handleLogout() {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadLocalData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        tvName.setText(pref.getString("customerName", "Khách hàng"));
        tvPhone.setText(pref.getString("customerPhone", ""));
        tvDob.setText(pref.getString("customerDob", ""));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK) loadData();
    }
}
