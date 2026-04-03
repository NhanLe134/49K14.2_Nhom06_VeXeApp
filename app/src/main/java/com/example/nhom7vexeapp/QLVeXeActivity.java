package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class QLVeXeActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {

    private RecyclerView rvTickets;
    private TicketAdapter adapter;
    private List<TicketModel> allTickets; // Lưu toàn bộ vé
    private List<TicketModel> currentDisplayList; // Vé đang hiển thị theo tab
    
    private TextView tabBooked, tabCompleted, tabCancelled, tvListTitle;
    private LinearLayout navHome, navSearch, navTickets, navFeedback;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_ve_xe);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        initViews();
        prepareData(); // Tạo dữ liệu mẫu
        setupRecyclerView();
        setupTabEvents(); // Xử lý nhấn tab
        setupBottomNavigation();
        
        // Mặc định chọn tab "Đã đặt"
        switchTab("Booked");
    }

    private void initViews() {
        rvTickets = findViewById(R.id.rvTickets);
        tvListTitle = findViewById(R.id.tvListTitle);
        
        // Tabs
        tabBooked = findViewById(R.id.tab_booked);
        tabCompleted = findViewById(R.id.tab_completed);
        tabCancelled = findViewById(R.id.tab_cancelled);
        
        // Footer Views
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets_btn);
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void prepareData() {
        allTickets = new ArrayList<>();
        // Vé Đã đặt
        allTickets.add(new TicketModel("07:00", "26/02/2026", "Đà Nẵng - Huế", "Long Hùng", "Số lượng ghế: 01", "Mã số ghế: B6", "Booked"));
        allTickets.add(new TicketModel("14:30", "28/02/2026", "Huế - Đà Nẵng", "Thành Công", "Số lượng ghế: 02", "Mã số ghế: A1, A2", "Booked"));
        
        // Vé Đã đi
        allTickets.add(new TicketModel("08:00", "20/02/2026", "Đà Nẵng - Huế", "Hải Vân", "Số lượng ghế: 01", "Mã số ghế: C3", "Completed"));
        
        // Vé Đã hủy
        allTickets.add(new TicketModel("10:00", "15/02/2026", "Đà Nẵng - Hội An", "Hùng Đức", "Số lượng ghế: 01", "Mã số ghế: D4", "Cancelled"));
        
        currentDisplayList = new ArrayList<>();
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
        
        // Reset màu các tab
        tabBooked.setTextColor(Color.parseColor("#888888"));
        tabBooked.setBackground(null);
        tabCompleted.setTextColor(Color.parseColor("#888888"));
        tabCompleted.setBackground(null);
        tabCancelled.setTextColor(Color.parseColor("#888888"));
        tabCancelled.setBackground(null);

        // Lọc dữ liệu và cập nhật giao diện tab được chọn
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

    @Override
    public void onTicketClick(TicketModel ticket) {
        // Xử lý khi nhấn vào vé
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
        if (navFeedback != null) {
            navFeedback.setOnClickListener(v -> {
                startActivity(new Intent(this, PhanHoiActivity.class));
            });
        }
    }
}
