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
        // Ánh xạ Avatar góc trên bên phải (imgOpProfile)
        imgOpProfile = findViewById(R.id.imgOpProfile);

        // Ánh xạ các nút ở thanh Bottom Navigation (Khớp với ID trong XML)
        navHome = findViewById(R.id.nav_home_op_main);
        navDriver = findViewById(R.id.nav_driver_op);
        navVehicle = findViewById(R.id.nav_vehicle_op);
        navTrip = findViewById(R.id.nav_trip_op);
        navRoute = findViewById(R.id.nav_route_op);
    }

    private void setupEvents() {
        // 1. Khi bấm vào Avatar (imgOpProfile)
        // Kết hợp: Ưu tiên dẫn vào trang QLNhaxeActivity của Xù để quản lý thông tin
        if (imgOpProfile != null) {
            imgOpProfile.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, QLNhaxeActivity.class);
                startActivity(intent);
            });
        }

        // 2. Tab "Phương tiện" (Màn hình trung gian của Xù)
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, PhuongTienManagementActivity.class);
                startActivity(intent);
            });
        }

        // 3. Tab "Chuyến xe" (Lấy từ code của bạn mới pull về)
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, TripListActivity.class);
                startActivity(intent);
            });
        }

        // 4. Tab "Tuyến xe" (Tất cả mọi người đều dùng chung)
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, QLTuyenxeActivity.class);
                startActivity(intent);
            });
        }

        // 5. Các tab bổ trợ (Trang chủ và Tài xế)
        if (navHome != null) {
            navHome.setOnClickListener(v ->
                    Toast.makeText(this, "Bạn đang ở Trang chủ", Toast.LENGTH_SHORT).show());
        }

        if (navDriver != null) {
            navDriver.setOnClickListener(v ->
                    Toast.makeText(this, "Chức năng Quản lý tài xế đang phát triển", Toast.LENGTH_SHORT).show());
        }
    }
}