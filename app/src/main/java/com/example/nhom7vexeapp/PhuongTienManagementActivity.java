package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PhuongTienManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phuongtien_management);

        // 1. Nút "Quản lý Loại xe"
        CardView btnCarType = findViewById(R.id.btnGoToCarType);
        if (btnCarType != null) {
            btnCarType.setOnClickListener(v -> {
                Intent intent = new Intent(PhuongTienManagementActivity.this, CarTypeManagementActivity.class);
                startActivity(intent);
            });
        }

        // 2. NÚT "QUẢN LÝ PHƯƠNG TIỆN"
        CardView btnManageVehicle = findViewById(R.id.btnManageVehicleMain);
        if (btnManageVehicle != null) {
            btnManageVehicle.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(PhuongTienManagementActivity.this, QLPhuongTienActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Không thể mở danh sách xe: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        // 3. Nút quay lại trên Toolbar
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Trang chủ -> OperatorMainActivity
        LinearLayout navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        
        // Tài xế -> DriverSelectionActivity (Đã sửa từ QLNhaxeActivity sang DriverSelectionActivity)
        LinearLayout navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> {
                Intent intent = new Intent(this, DriverSelectionActivity.class);
                startActivity(intent);
            });
        }

        // Chuyến xe -> TripListActivity
        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> startActivity(new Intent(this, TripListActivity.class)));
        }

        // Tuyến xe -> QLTuyenxeActivity
        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> startActivity(new Intent(this, QLTuyenxeActivity.class)));
        }
    }
}
