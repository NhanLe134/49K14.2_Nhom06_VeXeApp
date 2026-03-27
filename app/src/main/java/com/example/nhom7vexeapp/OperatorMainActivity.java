package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class OperatorMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_main);

        // Tìm ImageView Avatar qua ID imgOpProfile đã đặt trong layout
        ImageView btnProfile = findViewById(R.id.imgOpProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                // Chuyển sang màn hình Hồ sơ nhà xe (OperatorProfileActivity)
                Intent intent = new Intent(OperatorMainActivity.this, OperatorProfileActivity.class);
                startActivity(intent);
            });
        }

        // Chuyến xe navigation
        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, TripListActivity.class);
                startActivity(intent);
            });
        }

        // Tuyến xe navigation
        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                Intent intent = new Intent(OperatorMainActivity.this, QLTuyenxeActivity.class);
                startActivity(intent);
            });
        }
    }
}
