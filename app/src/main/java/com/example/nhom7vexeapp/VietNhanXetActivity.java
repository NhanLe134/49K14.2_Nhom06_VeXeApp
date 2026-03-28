package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VietNhanXetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viet_nhan_xet);

        // Ánh xạ View
        TextView tvBusName = findViewById(R.id.tvBusNameReview);
        TextView tvRoute = findViewById(R.id.tvRouteReview);
        TextView tvDateTime = findViewById(R.id.tvDateTimeReview);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText edtComment = findViewById(R.id.edtComment);
        Button btnSubmit = findViewById(R.id.btnSubmitReview);
        LinearLayout navHome = findViewById(R.id.nav_home_review);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            String busName = intent.getStringExtra("bus_company");
            String route = intent.getStringExtra("route");
            String dateTime = intent.getStringExtra("date_time");
            
            if (busName != null) tvBusName.setText(busName);
            if (route != null) tvRoute.setText(route);
            if (dateTime != null) tvDateTime.setText(dateTime);
        }

        // Xử lý sự kiện nút Gửi
        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = edtComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu dữ liệu vào danh sách static để hiển thị ở Tab Đã đánh giá
            PhanHoiActivity.listDaDanhGia.add(new PhanHoiActivity.FeedbackModel(
                tvBusName.getText().toString(),
                rating,
                comment,
                "28/03/2026", // Ngày hiện tại giả lập
                tvRoute.getText().toString()
            ));

            Toast.makeText(this, "Cảm ơn bạn đã đánh giá " + rating + " sao!", Toast.LENGTH_SHORT).show();
            
            // Quay lại màn hình trước đó (PhanHoiActivity)
            finish();
        });

        // Nút quay lại trang chủ
        navHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(VietNhanXetActivity.this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }
}
