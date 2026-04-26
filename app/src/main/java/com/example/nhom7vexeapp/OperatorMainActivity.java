package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OperatorMainActivity extends AppCompatActivity {

    private LinearLayout navHome, navDriver, navVehicle, navTrip, navRoute;
    private ImageView imgOpProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_main);

        initViews();    // Ánh xạ các thành phần giao diện
        setupEvents();   // Cài đặt sự kiện nút bấm
    }

    private void initViews() {
        // Ánh xạ Avatar góc trên bên phải
        imgOpProfile = findViewById(R.id.imgOpProfile);

        // Ánh xạ các nút ở thanh Bottom Navigation
        navHome = findViewById(R.id.nav_home_op_main);
        navDriver = findViewById(R.id.nav_driver_op);
        navVehicle = findViewById(R.id.nav_vehicle_op);
        navTrip = findViewById(R.id.nav_trip_op);
        navRoute = findViewById(R.id.nav_route_op);
    }

    private void setupEvents() {
        // 1. Khi bấm vào Avatar: Merge Logic
        if (imgOpProfile != null) {
            imgOpProfile.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, QLNhaxeActivity.class);
                startActivity(intent);
            });
        }


        // 2. Tab "Phương tiện" (PhuongTienManagementActivity)
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, PhuongTienManagementActivity.class);
                startActivity(intent);
            });
        }

        // 3. Tab "Chuyến xe" (TripListActivity)
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, TripListActivity.class);
                startActivity(intent);
            });
        }

        // 4. Tab "Tuyến xe" (QLTuyenxeActivity)
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, QLTuyenxeActivity.class);
                startActivity(intent);
            });
        }

        // 5. Tab "Tài xế": Dẫn vào DriverSelectionActivity (Quản lý tài xế)
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, DriverSelectionActivity.class);
                startActivity(intent);
            });
        }

        // 6. Tab "Trang chủ"
        if (navHome != null) {
            navHome.setOnClickListener(v ->
                    Toast.makeText(this, "Bạn đang ở Trang chủ quản trị", Toast.LENGTH_SHORT).show());
        }
    }
}