package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.KhachHang;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProfileActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvDob;
    private ImageView btnBack, imgAvatar;
    private View btnEditProfileImage; 
    private MaterialButton btnLogout;
    private LinearLayout navHome;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        initViews();
        setupEvents();
        loadData();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvProfileName);
        tvPhone = findViewById(R.id.tvProfilePhone);
        tvDob = findViewById(R.id.tvProfileDob);
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgProfileAvatar);
        btnEditProfileImage = findViewById(R.id.btnEditProfileImage); 
        btnLogout = findViewById(R.id.btnLogout);
        navHome = findViewById(R.id.nav_home_profile);

        // Ẩn các nút không có trong Figma
        View btnEditProfile = findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) btnEditProfile.setVisibility(View.GONE);
        
        View btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) btnDeleteAccount.setVisibility(View.GONE);
    }

    private void loadData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUid = pref.getString("customerUid", "");
        
        if (!currentUid.isEmpty()) {
            loadFromDatabase(currentUid);
        } else {
            // Hiển thị dữ liệu tạm từ login nếu không có UID
            tvName.setText(pref.getString("customerName", "Khách hàng"));
            tvPhone.setText(pref.getString("customerPhone", ""));
        }
    }

    private void loadFromDatabase(String uid) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getProfile(uid).enqueue(new Callback<KhachHang>() {
            @Override
            public void onResponse(Call<KhachHang> call, Response<KhachHang> response) {
                if (response.isSuccessful() && response.body() != null) {
                    KhachHang customer = response.body();
                    
                    // Cập nhật tên và ảnh (Sửa getHinhAnhURL thành getAnhDaiDienURL)
                    tvName.setText(customer.getHoTen() != null ? customer.getHoTen() : "Khách hàng");
                    String dob = customer.getNgaySinh();
                    if (dob != null && dob.contains("T")) dob = dob.split("T")[0];
                    tvDob.setText(dob != null ? dob : "Chưa cập nhật");
                    
                    if (customer.getAnhDaiDienURL() != null && !customer.getAnhDaiDienURL().isEmpty()) {
                        Glide.with(CustomerProfileActivity.this)
                                .load(customer.getAnhDaiDienURL())
                                .placeholder(R.drawable.account_circle)
                                .into(imgAvatar);
                    }

                    SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    tvPhone.setText(pref.getString("customerPhone", ""));
                }
            }
            @Override public void onFailure(Call<KhachHang> call, Throwable t) {
                Log.e("API_ERROR", "Load profile failed: " + t.getMessage());
            }
        });
    }

    private void setupEvents() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // ✅ NHẤN VÀO CÂY BÚT SẼ MỞ MÀN HÌNH CHỈNH SỬA THÔNG TIN
        if (btnEditProfileImage != null) {
            btnEditProfileImage.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditCustomerProfileActivity.class);
                startActivityForResult(intent, 300);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
        }

        if (navHome != null) navHome.setOnClickListener(v -> finish());
    }

    private void handleLogout() {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK) loadData();
    }
}
