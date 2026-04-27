package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.VehicleManaged;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleDetailActivity extends AppCompatActivity {

    private TextView tvPlate, tvType, tvSeats, tvStatus;
    private ImageView btnBack;
    private MaterialButton btnEdit;
    private VehicleManaged currentVehicle;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews();
        setupNavigation();

        // Nhận dữ liệu từ Intent với key đồng bộ từ Adapter
        if (getIntent() != null && getIntent().hasExtra("vehicle_managed_data")) {
            currentVehicle = (VehicleManaged) getIntent().getSerializableExtra("vehicle_managed_data");
            if (currentVehicle != null) {
                fetchAdditionalVehicleInfo();
            }
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditVehicleActivity.class);
                intent.putExtra("vehicle_managed_data", currentVehicle);
                startActivity(intent);
            });
        }
    }

    private void initViews() {
        tvPlate = findViewById(R.id.tvDetailPlate);
        tvType = findViewById(R.id.tvDetailType);
        tvSeats = findViewById(R.id.tvDetailSeats);
        tvStatus = findViewById(R.id.tvDetailStatus);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEditVehicle);
        
        View imgOpProfile = findViewById(R.id.imgOpProfile);
        if (imgOpProfile != null) {
            imgOpProfile.setOnClickListener(v -> startActivity(new Intent(this, OperatorProfileActivity.class)));
        }
    }

    private void fetchAdditionalVehicleInfo() {
        // Hiển thị biển số và trạng thái có sẵn từ model Xe
        tvPlate.setText(currentVehicle.getBienSoXe());
        tvStatus.setText(currentVehicle.getTrangThai());

        // Gọi API lấy danh sách loại xe để lấy Số Chỗ chính xác
        apiService.getLoaixe().enqueue(new Callback<List<Loaixe>>() {
            @Override
            public void onResponse(Call<List<Loaixe>> call, Response<List<Loaixe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String targetTypeId = currentVehicle.getLoaiXeIDStr();
                    for (Loaixe type : response.body()) {
                        if (type.getLoaixeID().equalsIgnoreCase(targetTypeId)) {
                            tvType.setText("Xe " + type.getSoCho() + " chỗ");
                            tvSeats.setText(String.valueOf(type.getSoCho()));
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<Loaixe>> call, Throwable t) {
                tvType.setText(currentVehicle.getLoaiXeIDStr());
                tvSeats.setText(currentVehicle.getSoGhe() != null ? String.valueOf(currentVehicle.getSoGhe()) : "N/A");
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.nav_home_op_main).setOnClickListener(v -> {
            startActivity(new Intent(this, OperatorMainActivity.class));
            finish();
        });
        findViewById(R.id.nav_driver_op).setOnClickListener(v -> {
            startActivity(new Intent(this, DriverSelectionActivity.class));
            finish();
        });
        findViewById(R.id.nav_vehicle_op).setOnClickListener(v -> finish());
        findViewById(R.id.nav_trip_op).setOnClickListener(v -> {
            startActivity(new Intent(this, TripListActivity.class));
            finish();
        });
        findViewById(R.id.nav_route_op).setOnClickListener(v -> {
            startActivity(new Intent(this, QLTuyenxeActivity.class));
            finish();
        });
    }
}
