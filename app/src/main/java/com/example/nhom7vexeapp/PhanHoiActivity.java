package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.TripSearchResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// #Trang
public class PhanHoiActivity extends AppCompatActivity {

    private LinearLayout layoutFeedbackList;
    private TextView tabPending, tabReviewed;
    private View tabIndicator;
    private SharedPreferences sharedPreferences;
    private String customerId;
    private ApiService apiService;

    private List<Map<String, Object>> allUserTickets = new ArrayList<>();
    private List<Map<String, Object>> allFeedbacks = new ArrayList<>();
    private Map<String, TripSearchResult> tripCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phan_hoi);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        customerId = sharedPreferences.getString("customerUid", "");
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupTabEvents();
        loadInitialData();
    }

    private void initViews() {
        layoutFeedbackList = findViewById(R.id.layout_feedback_list);
        tabPending = findViewById(R.id.tab_pending);
        tabReviewed = findViewById(R.id.tab_reviewed);
        tabIndicator = findViewById(R.id.tab_indicator);

        tabIndicator.post(() -> {
            View tabLayout = findViewById(R.id.tab_layout);
            if (tabLayout != null) {
                tabIndicator.getLayoutParams().width = tabLayout.getWidth() / 2;
                tabIndicator.requestLayout();
            }
        });
    }

    private void setupTabEvents() {
        tabPending.setOnClickListener(v -> showPendingFeedback());
        tabReviewed.setOnClickListener(v -> showReviewedFeedback());
        findViewById(R.id.nav_home).setOnClickListener(v -> finish());
        findViewById(R.id.nav_tickets).setOnClickListener(v -> startActivity(new Intent(this, QLVeXeActivity.class)));
    }

    private void loadInitialData() {
        if (customerId.isEmpty()) {
            showLoginRequiredDialog();
            return;
        }
        apiService.getChuyenXe().enqueue(new Callback<List<TripSearchResult>>() {
            @Override
            public void onResponse(Call<List<TripSearchResult>> call, Response<List<TripSearchResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tripCache.clear();
                    for (TripSearchResult t : response.body()) tripCache.put(t.getId(), t);
                }
                loadTicketsAndFeedbacks();
            }
            @Override public void onFailure(Call<List<TripSearchResult>> call, Throwable t) { loadTicketsAndFeedbacks(); }
        });
    }

    private void loadTicketsAndFeedbacks() {
        apiService.getAllTickets().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUserTickets.clear();
                    for (Map<String, Object> t : response.body()) {
                        if (customerId.equals(getFieldId(t.get("KhachHang")))) allUserTickets.add(t);
                    }
                    apiService.getFeedbacks().enqueue(new Callback<List<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                allFeedbacks.clear();
                                allFeedbacks.addAll(response.body());
                                showPendingFeedback();
                            }
                        }
                        @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) { showPendingFeedback(); }
                    });
                }
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(PhanHoiActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPendingFeedback() {
        tabPending.setTextColor(Color.BLACK);
        tabReviewed.setTextColor(Color.parseColor("#888888"));
        tabIndicator.animate().translationX(0).setDuration(200);
        layoutFeedbackList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        boolean hasData = false;
        for (Map<String, Object> ticket : allUserTickets) {
            String ticketId = getFieldId(ticket.get("VeID"));
            String trangThaiDanhGia = getString(ticket, "TrangThaiDanhGia");
            String ngayKetThuc = getString(ticket, "NgayKetThuc");

            boolean alreadyHasReview = false;
            for (Map<String, Object> fb : allFeedbacks) {
                if (ticketId.equals(getFieldId(fb.get("Ve")))) {
                    alreadyHasReview = true;
                    break;
                }
            }

            if (trangThaiDanhGia.equalsIgnoreCase("Chờ đánh giá") && !isOver7Days(ngayKetThuc) && !alreadyHasReview) {
                hasData = true;
                View itemView = inflater.inflate(R.layout.item_phan_hoi, layoutFeedbackList, false);
                TicketInfo info = extractTicketInfo(ticket);

                ((TextView) itemView.findViewById(R.id.tvBusCompany)).setText(info.busName);
                ((TextView) itemView.findViewById(R.id.tvRoute)).setText(info.route);
                ((TextView) itemView.findViewById(R.id.tvTimeRange)).setText(info.startTime + " - " + info.endTime);
                
                TextView tvStatus = itemView.findViewById(R.id.tvStatus);
                tvStatus.setText(info.tripStatus);
                if (info.tripStatus.contains("Hoàn thành")) tvStatus.setTextColor(Color.parseColor("#27AE60"));

                formatDate(info.date, itemView);

                itemView.findViewById(R.id.btnWriteReview).setOnClickListener(v -> {
                    Intent intent = new Intent(this, VietNhanXetActivity.class);
                    intent.putExtra("ticket_id", ticketId);
                    intent.putExtra("trip_id", getFieldId(ticket.get("ChuyenXe")));
                    intent.putExtra("bus_company", info.busName);
                    intent.putExtra("route", info.route);
                    intent.putExtra("date_time", info.date + "   " + info.startTime + " - " + info.endTime);
                    startActivity(intent);
                });
                layoutFeedbackList.addView(itemView);
            }
        }
        if (!hasData) showEmptyMessage("Không có chuyến xe nào chờ đánh giá.");
    }

    private void showReviewedFeedback() {
        tabPending.setTextColor(Color.parseColor("#888888"));
        tabReviewed.setTextColor(Color.BLACK);
        tabIndicator.animate().translationX(tabIndicator.getWidth()).setDuration(200);
        layoutFeedbackList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        boolean hasData = false;

        for (Map<String, Object> fb : allFeedbacks) {
            if (!customerId.equals(getFieldId(fb.get("KhachHang")))) continue;

            hasData = true;
            View itemView = inflater.inflate(R.layout.item_da_danh_gia, layoutFeedbackList, false);
            String ticketId = getFieldId(fb.get("Ve"));
            
            String busName = "Nhà xe";
            String date = "";
            for(Map<String, Object> t : allUserTickets) {
                if(ticketId.equals(getFieldId(t.get("VeID")))) {
                    TicketInfo info = extractTicketInfo(t);
                    busName = info.busName;
                    date = info.date;
                    break;
                }
            }

            ((TextView) itemView.findViewById(R.id.tvBusNameDone)).setText(busName);
            ((RatingBar) itemView.findViewById(R.id.ratingBarDone)).setRating(getFloat(fb, "Diemso"));
            ((TextView) itemView.findViewById(R.id.tvCommentDone)).setText(getString(fb, "Nhanxet"));
            ((TextView) itemView.findViewById(R.id.tvTravelDateDone)).setText("Ngày đi: " + date);
            layoutFeedbackList.addView(itemView);
        }
        if (!hasData) showEmptyMessage("Bạn chưa có đánh giá nào.");
    }

    private boolean isOver7Days(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date endDate = sdf.parse(dateStr);
            long diff = new Date().getTime() - endDate.getTime();
            return TimeUnit.MILLISECONDS.toDays(diff) >= 7;
        } catch (Exception e) { return false; }
    }

    private TicketInfo extractTicketInfo(Map<String, Object> ticket) {
        TicketInfo info = new TicketInfo();
        String tripId = getFieldId(ticket.get("ChuyenXe"));
        if (tripCache.containsKey(tripId)) {
            TripSearchResult t = tripCache.get(tripId);
            info.startTime = cleanTime(t.getTime());
            info.endTime = cleanTime(t.getEndTime());
            info.date = t.getDate();
            info.tripStatus = t.getStatus();
            info.busName = t.getNhaXeName();
            info.route = t.getTuyenXeName();
        }
        return info;
    }

    private String getFieldId(Object obj) {
        if (obj instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) obj;
            String[] keys = {"KhachHangID", "ChuyenXeID", "VeID", "id"};
            for (String k : keys) if (m.containsKey(k)) return String.valueOf(m.get(k));
        }
        return String.valueOf(obj);
    }

    private String getString(Map<?,?> map, String key) {
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : "";
    }

    private float getFloat(Map<?,?> map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).floatValue() : 0.0f;
    }

    private String cleanTime(String t) {
        if (t == null || t.length() < 5) return "00:00";
        return t.contains(":") ? t.substring(0, 5) : t;
    }

    private void formatDate(String d, View v) {
        if (d == null || !d.contains("-")) return;
        String[] p = d.split("-");
        if (p.length == 3) {
            ((TextView) v.findViewById(R.id.tvDay)).setText(p[2]);
            ((TextView) v.findViewById(R.id.tvMonthYear)).setText(p[1] + "/" + p[0]);
        }
    }

    private void showEmptyMessage(String m) {
        TextView tv = new TextView(this); tv.setText(m); tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(0, 150, 0, 0); tv.setTextColor(Color.GRAY); layoutFeedbackList.addView(tv);
    }

    private void showLoginRequiredDialog() {
        new AlertDialog.Builder(this).setTitle("Yêu cầu").setMessage("Vui lòng đăng nhập.")
                .setPositiveButton("OK", (d, w) -> finish()).show();
    }

    private static class TicketInfo { String busName, route, startTime, endTime, date, tripStatus; }

    @Override protected void onResume() { super.onResume(); loadInitialData(); }
}
