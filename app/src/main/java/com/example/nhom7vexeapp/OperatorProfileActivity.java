package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.google.android.material.button.MaterialButton;

import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorProfileActivity extends AppCompatActivity {

    private TextView tvOpNameHeader, tvOpNameDetail, tvOpRep, tvOpAddress, tvOpPhone, tvOpEmail;
    private MaterialButton btnEdit, btnLogout;
    private ImageView btnBack, imgOpBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_profile);

        initViews();
        
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String opUid = pref.getString("op_uid", "");

        if (opUid.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        loadOperatorDataFromDB(opUid);
        setupBottomNavigation();

        // ✅ KHẮC PHỤC TRIỆT ĐỂ NÚT BACK
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish(); // Đóng activity hiện tại để quay về màn hình trước đó
            });
        }

        if (btnEdit != null) btnEdit.setOnClickListener(v -> startActivityForResult(new Intent(this, EditOperatorProfileActivity.class), 100));
        if (btnLogout != null) btnLogout.setOnClickListener(v -> {
            pref.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    private void initViews() {
        tvOpNameHeader = findViewById(R.id.tvOpBusName);
        tvOpNameDetail = findViewById(R.id.tvOpNameDetail);
        tvOpRep = findViewById(R.id.tvOpRep);
        tvOpAddress = findViewById(R.id.tvOpAddress);
        tvOpPhone = findViewById(R.id.tvOpPhone);
        tvOpEmail = findViewById(R.id.tvOpEmail);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogoutOp);
        btnBack = findViewById(R.id.btnBack);
        imgOpBanner = findViewById(R.id.imgOpBanner); 
    }

    private void loadOperatorDataFromDB(String opUid) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getNhaXeDetail(opUid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    tvOpNameHeader.setText(findValue(data, "Tennhaxe", "TenNhaXe"));
                    tvOpNameDetail.setText(findValue(data, "Tennhaxe"));
                    tvOpRep.setText(findValue(data, "TenNguoiDaiDien", "NguoiDaiDien"));
                    tvOpAddress.setText(findValue(data, "DiaChiTruSo", "Diachitruso"));
                    tvOpPhone.setText(findValue(data, "SoDienThoai", "Sodienthoai"));
                    tvOpEmail.setText(findValue(data, "Email", "email"));

                    String imgData = findValue(data, "AnhDaiDien", "AnhDaiDienURL");
                    if (!imgData.isEmpty()) {
                        Glide.with(OperatorProfileActivity.this).load(imgData).placeholder(R.drawable.nhaxe_home).into(imgOpBanner);
                    }
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private String findValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null && !map.get(key).toString().equals("null")) return map.get(key).toString();
        }
        return "";
    }

    private void setupBottomNavigation() {
        View h = findViewById(R.id.nav_home_op); if (h == null) h = findViewById(R.id.nav_home_op_main);
        if (h != null) h.setOnClickListener(v -> { startActivity(new Intent(this, OperatorMainActivity.class)); finish(); });
        View t = findViewById(R.id.nav_trip_op); if (t != null) t.setOnClickListener(v -> { startActivity(new Intent(this, TripListActivity.class)); finish(); });
        View r = findViewById(R.id.nav_route_op); if (r != null) r.setOnClickListener(v -> { startActivity(new Intent(this, QLTuyenxeActivity.class)); finish(); });
        View v = findViewById(R.id.nav_vehicle_op); if (v != null) v.setOnClickListener(v1 -> { startActivity(new Intent(this, PhuongTienManagementActivity.class)); finish(); });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadOperatorDataFromDB(getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("op_uid", ""));
        }
    }
}
