package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.api.CustomerResponse;
import com.google.android.material.button.MaterialButton;

import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProfileActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvDob;
    private ImageView btnBack, btnEdit, imgAvatar;
    private MaterialButton btnLogout;
    private LinearLayout navHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        initViews();
        
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String customerUid = pref.getString("customerUid", "");
        if (customerUid.isEmpty()) customerUid = pref.getString("user_id", ""); 

        if (customerUid.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadAllData(customerUid);

        btnBack.setOnClickListener(v -> finish());
        if (navHome != null) navHome.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            pref.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        btnEdit.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, EditCustomerProfileActivity.class), 300);
        });
    }

    private void initViews() {
        tvName = findViewById(R.id.tvProfileName);
        tvPhone = findViewById(R.id.tvProfilePhone);
        tvDob = findViewById(R.id.tvProfileDob);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        navHome = findViewById(R.id.nav_home_profile);
        imgAvatar = findViewById(R.id.imgProfileAvatar);
    }

    private void loadAllData(String uid) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getKhachHangDetail(uid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    
                    // 1. Hiển thị Tên
                    String name = findValue(data, "Hovaten", "TenKhachHang", "name");
                    tvName.setText(name.isEmpty() ? "Khách hàng" : name);

                    // 2. Hiển thị Ngày sinh
                    String dob = findValue(data, "Ngaysinh", "NgaySinh");
                    if (!dob.isEmpty() && dob.contains("-")) {
                        try {
                            String[] parts = dob.split("T")[0].split("-");
                            if (parts.length == 3) dob = parts[2] + "/" + parts[1] + "/" + parts[0];
                        } catch (Exception e) {}
                    }
                    tvDob.setText(dob.isEmpty() ? "Chưa cập nhật" : dob);

                    // ✅ 3. HIỂN THỊ ẢNH ĐẠI DIỆN (Xử lý chuỗi Base64)
                    String imgData = findValue(data, "AnhDaiDien", "AnhDaiDienURL", "Avatar");
                    if (!imgData.isEmpty() && imgAvatar != null) {
                        // Nếu là chuỗi Base64 (bắt đầu bằng data:image), Glide sẽ tự xử lý hoặc load URL
                        Glide.with(CustomerProfileActivity.this)
                            .load(imgData)
                            .placeholder(R.drawable.nhaxe_home)
                            .error(R.drawable.account_circle)
                            .circleCrop()
                            .into(imgAvatar);
                    }
                    
                    // 4. Hiển thị SĐT
                    fetchPhoneFromAuth(uid);
                } else {
                    fetchPhoneFromAuth(uid);
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                fetchPhoneFromAuth(uid);
            }
        });
    }

    private void fetchPhoneFromAuth(String uid) {
        ApiClient.getClient().create(ApiService.class).getUserAuthDetail(uid).enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvPhone.setText(response.body().getSdt());
                }
            }
            @Override public void onFailure(Call<CustomerResponse> call, Throwable t) {}
        });
    }

    private String findValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key) && entry.getValue() != null) {
                    String val = entry.getValue().toString();
                    if (!val.equalsIgnoreCase("null") && !val.isEmpty()) return val;
                }
            }
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK) {
            loadAllData(getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("customerUid", ""));
        }
    }
}
