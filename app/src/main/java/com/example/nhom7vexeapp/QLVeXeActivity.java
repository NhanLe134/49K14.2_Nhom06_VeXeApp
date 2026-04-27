package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.adapters.TicketAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QLVeXeActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {

    private RecyclerView rvTickets;
    private TicketAdapter adapter;
    private List<TicketModel> allTickets = new ArrayList<>(); // Lưu toàn bộ vé từ API
    private List<TicketModel> currentDisplayList = new ArrayList<>(); // Vé đang hiển thị theo tab
    
    private TextView tabBooked, tabCompleted, tabCancelled, tvListTitle;
    private LinearLayout navHome, navSearch, navTickets, navFeedback;
    private SharedPreferences sharedPreferences;
    private String customerId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_ve_xe);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        customerId = sharedPreferences.getString("customerUid", "");
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupRecyclerView();
        setupTabEvents();
        setupBottomNavigation();
        
        loadTicketsFromServer();
    }

    private void initViews() {
        rvTickets = findViewById(R.id.rvTickets);
        tvListTitle = findViewById(R.id.tvListTitle);

        tabBooked = findViewById(R.id.tab_booked);
        tabCompleted = findViewById(R.id.tab_completed);
        tabCancelled = findViewById(R.id.tab_cancelled);

        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets_btn);
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void loadTicketsFromServer() {
        if (customerId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getAllTickets().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTickets.clear();
                    for (Map<String, Object> map : response.body()) {
                        String tKh = getFieldId(map.get("KhachHang"));
                        if (customerId.equals(tKh)) {
                            // Phân tích dữ liệu từ API để tạo TicketModel
                            Object tripObj = map.get("ChuyenXe");
                            String busName = "Nhà xe";
                            String route = "Tuyến đường";
                            if (tripObj instanceof Map) {
                                Map<?,?> trip = (Map<?,?>) tripObj;
                                busName = String.valueOf(trip.get("TenNhaxe") != null ? trip.get("TenNhaxe") : (trip.get("tenNhaxe") != null ? trip.get("tenNhaxe") : "Nhà xe"));
                                route = trip.get("DiemDi") + " - " + trip.get("DiemDen");
                            }

                            String time = String.valueOf(map.get("GioDi") != null ? map.get("GioDi") : "");
                            String date = String.valueOf(map.get("NgayDi") != null ? map.get("NgayDi") : "");
                            String status = String.valueOf(map.get("TrangThai") != null ? map.get("TrangThai") : "");
                            
                            // Chuyển đổi trạng thái API sang trạng thái App (Booked, Completed, Cancelled)
                            String appStatus = "Booked";
                            if ("Đã đi".equals(status) || "Completed".equalsIgnoreCase(status)) appStatus = "Completed";
                            else if ("Đã hủy".equals(status) || "Cancelled".equalsIgnoreCase(status)) appStatus = "Cancelled";

                            allTickets.add(new TicketModel(
                                time, date, route, busName, "Số lượng ghế: 01", "Mã ghế: " + map.get("VeID"), appStatus
                            ));
                        }
                    }
                    switchTab("Booked"); // Mặc định hiển thị vé đã đặt
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e("QLVeXe", "Error: " + t.getMessage());
            }
        });
    }

    private String getFieldId(Object obj) {
        if (obj == null) return "";
        if (obj instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) obj;
            if (m.containsKey("KhachHangID")) return String.valueOf(m.get("KhachHangID"));
            if (m.containsKey("id")) return String.valueOf(m.get("id"));
        }
        return String.valueOf(obj);
    }

    private void setupRecyclerView() {
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(this, currentDisplayList, this);
        rvTickets.setAdapter(adapter);
    }

    private void setupTabEvents() {
        tabBooked.setOnClickListener(v -> switchTab("Booked"));
        tabCompleted.setOnClickListener(v -> switchTab("Completed"));
        tabCancelled.setOnClickListener(v -> switchTab("Cancelled"));
    }

    private void switchTab(String status) {
        currentDisplayList.clear();

        tabBooked.setTextColor(Color.parseColor("#888888"));
        tabBooked.setBackground(null);
        tabCompleted.setTextColor(Color.parseColor("#888888"));
        tabCompleted.setBackground(null);
        tabCancelled.setTextColor(Color.parseColor("#888888"));
        tabCancelled.setBackground(null);

        if (status.equals("Booked")) {
            tvListTitle.setText("Danh sách vé đã đặt");
            tabBooked.setTextColor(Color.BLACK);
            tabBooked.setBackgroundResource(R.drawable.tab_selected_border);
        } else if (status.equals("Completed")) {
            tvListTitle.setText("Danh sách vé đã đi");
            tabCompleted.setTextColor(Color.BLACK);
            tabCompleted.setBackgroundResource(R.drawable.tab_selected_border);
        } else {
            tvListTitle.setText("Danh sách vé đã hủy");
            tabCancelled.setTextColor(Color.BLACK);
            tabCancelled.setBackgroundResource(R.drawable.tab_selected_border);
        }

        for (TicketModel t : allTickets) {
            if (t.getStatus().equals(status)) {
                currentDisplayList.add(t);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        if (navHome != null) navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        if (navSearch != null) navSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchTicketActivity.class)));
        if (navFeedback != null) navFeedback.setOnClickListener(v -> startActivity(new Intent(this, PhanHoiActivity.class)));
    }

    @Override
    public void onTicketClick(TicketModel ticket) {
        showTicketDetailDialog(ticket);
    }

    private void showTicketDetailDialog(TicketModel ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ticket_detail, null);

        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvSeatCount = view.findViewById(R.id.tvSeatCount);
        TextView tvSeatNumber = view.findViewById(R.id.tvSeatNumber);
        TextView tvStatusDetail = view.findViewById(R.id.tvStatusDetail);
        ImageView btnClose = view.findViewById(R.id.btnClose);

        tvDate.setText("Ngày khởi hành: " + ticket.getDate());
        tvTime.setText("Giờ khởi hành: " + ticket.getTime());
        tvSeatCount.setText(ticket.getSeatCount());
        tvSeatNumber.setText(ticket.getSeats());

        String statusText = "Trạng thái: ";
        if (ticket.getStatus().equals("Booked")) statusText += "Đã đặt";
        else if (ticket.getStatus().equals("Completed")) statusText += "Đã đi";
        else statusText += "Đã hủy";
        tvStatusDetail.setText(statusText);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
