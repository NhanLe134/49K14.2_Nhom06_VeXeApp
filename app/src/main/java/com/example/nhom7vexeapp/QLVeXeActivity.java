package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class QLVeXeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_ve_xe);

        // Ánh xạ các nút ở thanh menu dưới cùng
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navFeedback = findViewById(R.id.nav_feedback);

        // Chuyển về trang chủ
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(QLVeXeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // Chuyển sang trang đánh giá (phản hồi)
        if (navFeedback != null) {
            navFeedback.setOnClickListener(v -> {
                Intent intent = new Intent(QLVeXeActivity.this, PhanHoiActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
