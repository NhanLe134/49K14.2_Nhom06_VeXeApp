package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.NhaXe;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// #Trang
public class OperatorProfileActivity extends AppCompatActivity {

    private static final String TAG = "OperatorProfile";
    private TextView tvOpNameHeader, tvOpNameDetail, tvOpRep, tvOpAddress, tvOpPhone, tvOpEmail;
    private MaterialButton btnEdit, btnLogout;
    private ImageView btnBack, imgOpBanner;
    private String opUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_profile);

        initViews();
        
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        opUid = pref.getString("op_uid", "");

        if (opUid.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupEvents();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật dữ liệu ngay khi quay lại màn hình này
        if (opUid != null && !opUid.isEmpty()) {
            loadOperatorDataFromDB(opUid);
        }
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

    private void setupEvents() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditOperatorProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadOperatorDataFromDB(String id) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getNhaXeDetail(id).enqueue(new Callback<NhaXe>() {
            @Override
            public void onResponse(Call<NhaXe> call, Response<NhaXe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NhaXe data = response.body();
                    updateUI(data);
                }
            }
            @Override public void onFailure(Call<NhaXe> call, Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
            }
        });
    }

    private void updateUI(NhaXe data) {
        if (data == null) return;
        
        tvOpNameHeader.setText(nonNull(data.getBusName(), "Nhà xe"));
        tvOpNameDetail.setText(nonNull(data.getBusName(), "Chưa có tên"));
        tvOpRep.setText(nonNull(data.getRepresentative(), "Chưa cập nhật"));
        tvOpAddress.setText(nonNull(data.getAddress(), "Chưa cập nhật"));
        tvOpPhone.setText(nonNull(data.getPhone(), "Chưa cập nhật"));
        if (tvOpEmail != null) {
            tvOpEmail.setText(nonNull(data.getEmail(), "Chưa cập nhật"));
        }

        String imgUrl = data.getBannerUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.nhaxe_home)
                .error(R.drawable.nhaxe_home)
                .into(imgOpBanner);
        }
    }

    private String nonNull(String val, String def) {
        return (val == null || val.isEmpty() || val.equals("null")) ? def : val;
    }

    private void setupBottomNavigation() {
        View h = findViewById(R.id.nav_home_op_main);
        if (h != null) h.setOnClickListener(v -> {
            startActivity(new Intent(this, OperatorMainActivity.class));
            finish();
        });
        
        View d = findViewById(R.id.nav_driver_op);
        if (d != null) d.setOnClickListener(v -> {
            startActivity(new Intent(this, QLNhaxeActivity.class));
            finish();
        });

        View v = findViewById(R.id.nav_vehicle_op);
        if (v != null) v.setOnClickListener(v1 -> {
            startActivity(new Intent(this, PhuongTienManagementActivity.class));
            finish();
        });

        View t = findViewById(R.id.nav_trip_op);
        if (t != null) t.setOnClickListener(v2 -> {
            startActivity(new Intent(this, TripListActivity.class));
            finish();
        });

        View r = findViewById(R.id.nav_route_op);
        if (r != null) r.setOnClickListener(v3 -> {
            startActivity(new Intent(this, QLTuyenxeActivity.class));
            finish();
        });
    }
}
