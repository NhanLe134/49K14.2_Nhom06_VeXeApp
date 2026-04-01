package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView btnProfile;
    // Khai báo đủ 4 nút ở thanh Navbar
    private LinearLayout navHome, navSearch, navTickets, navFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();    // 1. Ánh xạ các nút
        setupEvents();   // 2. Cài đặt sự kiện chuyển trang
    }

    private void initViews() {
        btnProfile = findViewById(R.id.btnProfile);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets); // Nút Vé của tôi của Xù đây
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void setupEvents() {
        // 1. Xử lý nút Profile (Avatar)
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                boolean isLoggedIn = pref.getBoolean("isLoggedIn", false);
                if (isLoggedIn) {
                    startActivity(new Intent(MainActivity.this, CustomerProfileActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }

        // 2. Nút Tìm kiếm vé
        if (navSearch != null) {
            navSearch.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SearchTicketActivity.class));
            });
        }

        // 3. Nút Vé của tôi (CHỖ XÙ ĐANG CẦN ĐÂY)
        if (navTickets != null) {
            navTickets.setOnClickListener(v -> {
                // Chuyển sang màn hình Quản lý vé xe
                Intent intent = new Intent(MainActivity.this, QLVeXeActivity.class);
                startActivity(intent);
            });
        }

        // 4. Nút Đánh giá (Phản hồi)
        if (navFeedback != null) {
            navFeedback.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, PhanHoiActivity.class));
            });
        }

        // 5. Nút Trang chủ (Tạm thời chỉ hiện thông báo hoặc reset lại trang)
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Đã ở trang chủ rồi nên không cần chuyển trang nữa
            });
        }
    }
}