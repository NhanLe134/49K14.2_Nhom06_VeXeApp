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

        // 2. NÚT "QUẢN LÝ PHƯƠNG TIỆN" - Sửa lỗi chuyển màn hình
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
        LinearLayout navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        
        LinearLayout navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> startActivity(new Intent(this, QLNhaxeActivity.class)));
        }

        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> startActivity(new Intent(this, TripListActivity.class)));
        }

        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> startActivity(new Intent(this, QLTuyenxeActivity.class)));
        }
    }
}
