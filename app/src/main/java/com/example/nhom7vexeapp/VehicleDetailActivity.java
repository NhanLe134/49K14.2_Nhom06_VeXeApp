package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.models.Vehicle;
import com.google.android.material.button.MaterialButton;

public class VehicleDetailActivity extends AppCompatActivity {

    private TextView tvPlate, tvType, tvSeats, tvStatus;
    private ImageView btnBack;
    private MaterialButton btnEdit;
    private Vehicle currentVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_detail);

        initViews();

        // Nhận dữ liệu từ Intent an toàn
        if (getIntent() != null && getIntent().hasExtra("vehicle_data")) {
            Object data = getIntent().getSerializableExtra("vehicle_data");
            if (data instanceof Vehicle) {
                currentVehicle = (Vehicle) data;
                if (tvPlate != null) tvPlate.setText(currentVehicle.getPlateNumber());
                if (tvType != null) tvType.setText(currentVehicle.getType());
                if (tvSeats != null) tvSeats.setText(String.valueOf(currentVehicle.getSeatCount()));
                if (tvStatus != null) tvStatus.setText(currentVehicle.getStatus());
            }
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                // Chuyển sang màn hình Chỉnh sửa (EditVehicleActivity)
                Intent intent = new Intent(VehicleDetailActivity.this, EditVehicleActivity.class);
                intent.putExtra("vehicle_data", currentVehicle);
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
    }
}
