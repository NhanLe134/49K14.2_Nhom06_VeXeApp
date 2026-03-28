package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Nạp layout activity_main
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần giao diện
        LinearLayout navFeedback = findViewById(R.id.nav_feedback);
        ImageView btnProfile = findViewById(R.id.btnProfile);
        
        // Xử lý sự kiện click cho nút Đánh giá
        if (navFeedback != null) {
            navFeedback.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, PhanHoiActivity.class);
                startActivity(intent);
            });
        }

        // Xử lý sự kiện click cho nút Profile (Đăng nhập)
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }
    }
}
