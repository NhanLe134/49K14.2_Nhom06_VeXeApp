package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class PhanHoiActivity extends AppCompatActivity {

    public static class FeedbackModel {
        public String busName;
        public float rating;
        public String comment;
        public String date;
        public String route;

        public FeedbackModel(String busName, float rating, String comment, String date, String route) {
            this.busName = busName;
            this.rating = rating;
            this.comment = comment;
            this.date = date;
            this.route = route;
        }
    }

    public static List<FeedbackModel> listDaDanhGia = new ArrayList<>();

    private LinearLayout layoutFeedbackList;
    private TextView tabPending, tabReviewed;
    private View tabIndicator;
    private LinearLayout navHome, navSearch, navTickets, navFeedback;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phan_hoi);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        initViews();
        setupBottomNavigation();
        setupTabEvents();

        // Mặc định hiển thị tab Chờ đánh giá
        showPendingFeedback();
    }

    private void initViews() {
        layoutFeedbackList = findViewById(R.id.layout_feedback_list);
        tabPending = findViewById(R.id.tab_pending);
        tabReviewed = findViewById(R.id.tab_reviewed);
        tabIndicator = findViewById(R.id.tab_indicator);

        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets);
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void setupTabEvents() {
        tabPending.setOnClickListener(v -> showPendingFeedback());
        tabReviewed.setOnClickListener(v -> showReviewedFeedback());
    }

    private void showPendingFeedback() {
        // Cập nhật UI tab
        tabPending.setTextColor(Color.BLACK);
        tabReviewed.setTextColor(Color.parseColor("#888888"));
        tabIndicator.animate().x(0).setDuration(200);

        layoutFeedbackList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        // Tạo dữ liệu mẫu cho Chờ đánh giá
        String[][] pendingData = {
            {"Khang Limousine", "Huế - Đà Nẵng", "13", "04/2026", "13:45 - 16:45"},
            {"Hải Vân", "Đà Nẵng - Huế", "10", "04/2026", "08:00 - 11:00"}
        };

        for (String[] data : pendingData) {
            View itemView = inflater.inflate(R.layout.item_phan_hoi, layoutFeedbackList, false);
            ((TextView) itemView.findViewById(R.id.tvBusCompany)).setText(data[0]);
            ((TextView) itemView.findViewById(R.id.tvRoute)).setText(data[1]);
            ((TextView) itemView.findViewById(R.id.tvDay)).setText(data[2]);
            ((TextView) itemView.findViewById(R.id.tvMonthYear)).setText(data[3]);
            ((TextView) itemView.findViewById(R.id.tvTimeRange)).setText(data[4]);

            itemView.findViewById(R.id.btnWriteReview).setOnClickListener(v -> {
                Intent intent = new Intent(this, VietNhanXetActivity.class);
                intent.putExtra("bus_company", data[0]);
                intent.putExtra("route", data[1]);
                intent.putExtra("date_time", data[4] + " " + data[2] + "/" + data[3]);
                startActivity(intent);
            });

            layoutFeedbackList.addView(itemView);
        }
    }

    private void showReviewedFeedback() {
        // Cập nhật UI tab
        tabPending.setTextColor(Color.parseColor("#888888"));
        tabReviewed.setTextColor(Color.BLACK);
        float tabWidth = tabPending.getWidth();
        tabIndicator.animate().x(tabWidth).setDuration(200);

        layoutFeedbackList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (listDaDanhGia.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Bạn chưa có đánh giá nào.");
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            tvEmpty.setPadding(0, 100, 0, 0);
            layoutFeedbackList.addView(tvEmpty);
        } else {
            for (FeedbackModel fb : listDaDanhGia) {
                View itemView = inflater.inflate(R.layout.item_da_danh_gia, layoutFeedbackList, false);
                ((TextView) itemView.findViewById(R.id.tvBusNameDone)).setText(fb.busName);
                ((RatingBar) itemView.findViewById(R.id.ratingBarDone)).setRating(fb.rating);
                ((TextView) itemView.findViewById(R.id.tvCommentDone)).setText(fb.comment);
                ((TextView) itemView.findViewById(R.id.tvTravelDateDone)).setText("Ngày đi: " + fb.date);
                layoutFeedbackList.addView(itemView);
            }
        }
    }

    private void setupBottomNavigation() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }
        if (navSearch != null) {
            navSearch.setOnClickListener(v -> {
                startActivity(new Intent(this, SearchTicketActivity.class));
            });
        }
        if (navTickets != null) {
            navTickets.setOnClickListener(v -> {
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                if (isLoggedIn) {
                    startActivity(new Intent(this, QLVeXeActivity.class));
                } else {
                    showLoginRequiredDialog();
                }
            });
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

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại danh sách nếu vừa viết đánh giá xong
        if (tabReviewed.getCurrentTextColor() == Color.BLACK) {
            showReviewedFeedback();
        }
    }
}
