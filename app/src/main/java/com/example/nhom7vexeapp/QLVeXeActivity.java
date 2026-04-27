package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.adapters.TicketAdapter;
import com.example.nhom7vexeapp.models.Ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QLVeXeActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {

    private RecyclerView rvTickets;
    private TicketAdapter adapter;
    private List<Ticket> ticketList = new ArrayList<>();
    private TextView tabBooked, tabCompleted, tabCancelled, tvListTitle, tvEmptyMessage;
    private ProgressBar progressBar;
    private String currentStatus = "Đã đặt";
    private String customerId;
    private ApiService apiService;
    private ImageView btnProfileHeader;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_ve_xe);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        customerId = sharedPreferences.getString("customerUid", "");
        if (customerId.isEmpty()) customerId = sharedPreferences.getString("user_id", "");

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupTabs();
        setupBottomNavigation();
        loadTickets();
    }

    private void initViews() {
        rvTickets = findViewById(R.id.rvTickets);
        tabBooked = findViewById(R.id.tab_booked);
        tabCompleted = findViewById(R.id.tab_completed);
        tabCancelled = findViewById(R.id.tab_cancelled);
        tvListTitle = findViewById(R.id.tvListTitle);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        progressBar = findViewById(R.id.progressBar);
        btnProfileHeader = findViewById(R.id.btnProfileHeader);

        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(this, ticketList, this);
        rvTickets.setAdapter(adapter);

        if (btnProfileHeader != null) {
            btnProfileHeader.setOnClickListener(v -> {
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                if (isLoggedIn) {
                    startActivity(new Intent(this, CustomerProfileActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
            });
        }
    }

    private void setupTabs() {
        tabBooked.setOnClickListener(v -> switchTab("Đã đặt", tabBooked, "Danh sách vé đã đặt"));
        tabCompleted.setOnClickListener(v -> switchTab("Đã đi", tabCompleted, "Lịch sử chuyến đi"));
        tabCancelled.setOnClickListener(v -> switchTab("Đã hủy", tabCancelled, "Vé đã hủy"));
    }

    private void switchTab(String status, TextView selectedTab, String title) {
        currentStatus = status;
        tvListTitle.setText(title);

        tabBooked.setTextColor(Color.parseColor("#888888"));
        tabBooked.setBackground(null);
        tabCompleted.setTextColor(Color.parseColor("#888888"));
        tabCompleted.setBackground(null);
        tabCancelled.setTextColor(Color.parseColor("#888888"));
        tabCancelled.setBackground(null);

        selectedTab.setTextColor(Color.BLACK);
        selectedTab.setBackgroundResource(R.drawable.tab_selected_border);

        loadTickets();
    }

    private void loadTickets() {
        if (customerId.isEmpty()) {
            tvEmptyMessage.setText("Vui lòng đăng nhập để xem vé");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyMessage.setVisibility(View.GONE);

        apiService.getTickets(customerId, currentStatus).enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ticketList.clear();
                    ticketList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (ticketList.isEmpty()) {
                        tvEmptyMessage.setText("Bạn chưa có đơn đặt vé nào.");
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(QLVeXeActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QLVeXeActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_search).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchTicketActivity.class));
            finish();
        });
        findViewById(R.id.nav_feedback).setOnClickListener(v -> {
            startActivity(new Intent(this, PhanHoiActivity.class));
            finish();
        });
    }

    @Override
    public void onTicketClick(Ticket ticket) { }

    @Override
    public void onCancelClick(Ticket ticket) {
        if (ticket.getVeID() == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("TrangThai", "Đã hủy");
        apiService.patchTicket(ticket.getVeID(), data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(QLVeXeActivity.this, "Đã hủy vé thành công", Toast.LENGTH_SHORT).show();
                    loadTickets();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
