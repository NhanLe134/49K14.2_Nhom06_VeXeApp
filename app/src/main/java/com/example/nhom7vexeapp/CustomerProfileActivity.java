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
        
        // Hiển thị ảnh cục bộ nếu có
        String localUri = pref.getString("localAvatarUri", "");
        if (!localUri.isEmpty() && imgAvatar != null) {
            Glide.with(this).load(Uri.parse(localUri)).placeholder(R.drawable.logo).into(imgAvatar);
        }

        String customerUid = pref.getString("customerUid", "");
        String khachHangID = pref.getString("khachHangID", "");
        
        if (!customerUid.isEmpty()) {
            loadFromDatabase(customerUid);
        } else if (!khachHangID.isEmpty()) {
            viewModel.getProfile(khachHangID);
        } else {
            loadLocalData();
        }
    }

    private void loadFromDatabase(String uid) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getKhachHangDetail(uid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    if (data.containsKey("hoTen")) tvName.setText(String.valueOf(data.get("hoTen")));
                    if (data.containsKey("Ngaysinh")) tvDob.setText(String.valueOf(data.get("Ngaysinh")));
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
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
        tvPhone.setText(pref.getString("customerPhone", "0916441979"));
        tvDob.setText(pref.getString("customerDob", "20/11/2004"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK) loadData();
    }
}
