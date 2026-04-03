package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.adapters.VehicleAdapter;
import com.example.nhom7vexeapp.models.Vehicle;
import java.util.ArrayList;
import java.util.List;

public class QLPhuongTienActivity extends AppCompatActivity {

    private RecyclerView rvVehicles;
    private VehicleAdapter adapter;
    private List<Vehicle> vehicleList;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_ql_phuong_tien);

            btnBack = findViewById(R.id.btnBack);
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            rvVehicles = findViewById(R.id.rvVehicles);
            if (rvVehicles != null) {
                rvVehicles.setLayoutManager(new LinearLayoutManager(this));
                
                // Chuẩn bị dữ liệu mẫu
                vehicleList = new ArrayList<>();
                vehicleList.add(new Vehicle("75B-98603", "Limousine", "Đang bảo trì", 9));
                vehicleList.add(new Vehicle("43A-14824", "Limousine", "Hoạt động", 9));
                vehicleList.add(new Vehicle("51G-92372", "Xe 4 chỗ", "Tạm dừng", 4));
                vehicleList.add(new Vehicle("43B-50812", "Limousine", "Tạm dừng", 9));
                vehicleList.add(new Vehicle("75A-54592", "Limousine", "Tạm dừng", 9));
                vehicleList.add(new Vehicle("43B-63614", "Xe 4 chỗ", "Hoạt động", 4));

                adapter = new VehicleAdapter(vehicleList);
                rvVehicles.setAdapter(adapter);
            }

            // Thiết lập Bottom Navigation
            setupBottomNavigation();

        } catch (Exception e) {
            Log.e("QLPhuongTien", "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Lỗi khởi động: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupBottomNavigation() {
        // Tab Trang chủ
        LinearLayout navHome = findViewById(R.id.nav_home_op);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        // Tab Tài xế
        LinearLayout navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> {
                startActivity(new Intent(this, QLNhaxeActivity.class));
            });
        }

        // Tab Chuyến xe
        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                startActivity(new Intent(this, TripListActivity.class));
            });
        }

        // Tab Tuyến xe
        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                startActivity(new Intent(this, QLTuyenxeActivity.class));
            });
        }

        // Tab Phương tiện (Hiện tại - Click để reload hoặc cuộn lên đầu)
        LinearLayout navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                if (rvVehicles != null) rvVehicles.smoothScrollToPosition(0);
            });
        }
    }
}
