package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class PhanHoiActivity extends AppCompatActivity {

    private TextView tabPending, tabReviewed;
    private View tabIndicator;
    private LinearLayout layoutFeedbackList;

    // Danh sách lưu trữ đánh giá tạm thời (Mock Data)
    public static List<FeedbackModel> listDaDanhGia = new ArrayList<>();

    public static class FeedbackModel {
        public String company, comment, date, route;
        public float rating;
        public FeedbackModel(String c, float r, String cm, String d, String rt) {
            this.company = c; this.rating = r; this.comment = cm; this.date = d; this.route = rt;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phan_hoi);

        tabPending = findViewById(R.id.tab_pending);
        tabReviewed = findViewById(R.id.tab_reviewed);
        tabIndicator = findViewById(R.id.tab_indicator);
        layoutFeedbackList = findViewById(R.id.layout_feedback_list);
        LinearLayout navHome = findViewById(R.id.nav_home);

        showPendingFeedback();

        tabPending.setOnClickListener(v -> showPendingFeedback());
        tabReviewed.setOnClickListener(v -> showReviewedFeedback());

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(PhanHoiActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showPendingFeedback() {
        updateTabUI(tabPending);
        layoutFeedbackList.removeAllViews();
        addFeedbackItem("13", "04/2026", "13:45 - 16:45", "Khang Limousine", "Huế - Đà Nẵng", "Đã hoàn thành", "#27AE60");
        addFeedbackItem("06", "03/2026", "13:45 - 16:45", "Khang Limousine", "Đà Nẵng - Huế", "Đã hoàn thành", "#27AE60");
        addFeedbackItem("20", "02/2026", "13:45 - 16:45", "Khang Limousine", "Huế - Đà Nẵng", "Đang đón khách", "#F1C40F");
    }

    private void showReviewedFeedback() {
        updateTabUI(tabReviewed);
        layoutFeedbackList.removeAllViews();
        
        // 1. Dữ liệu mẫu ban đầu
        addReviewedItem("Khang Limousine", 5, "Tài xế thân thiện, mình được đón tận nhà. Xe chạy đúng giờ. Ghế ngồi thoải mái", "13/04/2026");
        
        // 2. Dữ liệu mới vừa được người dùng gửi từ VietNhanXetActivity
        for (FeedbackModel item : listDaDanhGia) {
            addReviewedItem(item.company, item.rating, item.comment, item.date);
        }
    }

    private void addFeedbackItem(String day, String monthYear, String time, String company, String route, String status, String statusColor) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_phan_hoi, layoutFeedbackList, false);
        ((TextView) itemView.findViewById(R.id.tvDay)).setText(day);
        ((TextView) itemView.findViewById(R.id.tvMonthYear)).setText(monthYear);
        ((TextView) itemView.findViewById(R.id.tvTimeRange)).setText(time);
        ((TextView) itemView.findViewById(R.id.tvBusCompany)).setText(company);
        ((TextView) itemView.findViewById(R.id.tvRoute)).setText(route);
        TextView tvStatus = itemView.findViewById(R.id.tvStatus);
        tvStatus.setText(status);
        tvStatus.setTextColor(Color.parseColor(statusColor));

        View btnWriteReview = itemView.findViewById(R.id.btnWriteReview);
        if (btnWriteReview != null) {
            btnWriteReview.setOnClickListener(v -> {
                Intent intent = new Intent(PhanHoiActivity.this, VietNhanXetActivity.class);
                intent.putExtra("bus_company", company);
                intent.putExtra("route", route);
                intent.putExtra("date_time", day + "/" + monthYear + "  " + time);
                startActivity(intent);
            });
        }
        layoutFeedbackList.addView(itemView);
    }

    private void addReviewedItem(String company, float rating, String comment, String date) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_da_danh_gia, layoutFeedbackList, false);
        ((TextView) itemView.findViewById(R.id.tvBusNameDone)).setText(company);
        ((RatingBar) itemView.findViewById(R.id.ratingBarDone)).setRating(rating);
        ((TextView) itemView.findViewById(R.id.tvCommentDone)).setText(comment);
        ((TextView) itemView.findViewById(R.id.tvTravelDateDone)).setText("Ngày đi: " + date);
        layoutFeedbackList.addView(itemView);
    }

    private void updateTabUI(TextView selectedTab) {
        tabPending.setTextColor(Color.parseColor("#888888"));
        tabReviewed.setTextColor(Color.parseColor("#888888"));
        selectedTab.setTextColor(Color.parseColor("#000000"));
        tabIndicator.post(() -> {
            int width = tabPending.getWidth();
            if (selectedTab == tabPending) tabIndicator.setTranslationX(0);
            else if (selectedTab == tabReviewed) tabIndicator.setTranslationX(width);
        });
    }
}
