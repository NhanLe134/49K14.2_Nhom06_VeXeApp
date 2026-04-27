package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView btnProfile;
    private LinearLayout navHome, navSearch, navTickets, navFeedback;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        initViews();
        setupEvents();
    }

    private void initViews() {
        btnProfile = findViewById(R.id.btnProfile);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets);
        if (navTickets == null) navTickets = findViewById(R.id.nav_tickets_btn);
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void setupEvents() {
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                // Kiểm tra cờ đăng nhập
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                if (isLoggedIn) {
                    startActivity(new Intent(MainActivity.this, CustomerProfileActivity.class));
                } else {
                    // Nếu chưa đăng nhập -> Chuyển sang màn hình Login
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }

        if (navSearch != null) {
            navSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchTicketActivity.class)));
        }

        if (navTickets != null) {
            navTickets.setOnClickListener(v -> checkLoginAndNavigate(QLVeXeActivity.class));
        }

        if (navFeedback != null) {
            navFeedback.setOnClickListener(v -> checkLoginAndNavigate(PhanHoiActivity.class));
        }
    }

    private void checkLoginAndNavigate(Class<?> targetActivity) {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            startActivity(new Intent(this, targetActivity));
        } else {
            showLoginRequiredDialog();
        }
    }

    private void showLoginRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage("Bạn cần đăng nhập để thực hiện chức năng này.")
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Để sau", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
