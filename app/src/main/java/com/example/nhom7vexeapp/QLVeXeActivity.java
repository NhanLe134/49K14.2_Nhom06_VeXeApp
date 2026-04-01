package com.example.nhom7vexeapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class QLVeXeActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {

    private RecyclerView rvTickets;
    private TicketAdapter ticketAdapter;
    private List<TicketModel> fullTicketList = new ArrayList<>();
    private List<TicketModel> displayedList = new ArrayList<>();
    
    private TextView tabBooked, tabCompleted, tabCancelled, tvListTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_ve_xe);

        initViews();
        setupRecyclerView();
        loadMockData();
        filterTickets("Booked"); // Mặc định hiện vé Đã đặt

        setupTabListeners();
        setupNavigationListeners();
    }

    private void initViews() {
        rvTickets = findViewById(R.id.rvTickets);
        tabBooked = findViewById(R.id.tab_booked);
        tabCompleted = findViewById(R.id.tab_completed);
        tabCancelled = findViewById(R.id.tab_cancelled);
        tvListTitle = findViewById(R.id.tvListTitle);
    }

    private void setupRecyclerView() {
        ticketAdapter = new TicketAdapter(this, displayedList, this);
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(ticketAdapter);
    }

    private void loadMockData() {
        // Vé đã đặt
        fullTicketList.add(new TicketModel("07:00", "26/02/2026", "Đà Nẵng - Huế", "Long Hùng", "01", "B6", "Booked"));
        
        // Vé đã đi
        fullTicketList.add(new TicketModel("15:15", "30/01/2026", "Đà Nẵng - Huế", "Long Hùng", "02", "C6 - C7", "Completed"));
        fullTicketList.add(new TicketModel("10:20", "18/01/2026", "Huế - Đà Nẵng", "Châu Thanh", "03", "A1 - A2 - A3", "Completed"));
        
        // Vé đã hủy
        fullTicketList.add(new TicketModel("12:30", "26/11/2025", "Đà Nẵng - Huế", "Phương Trang", "01", "C6", "Cancelled"));
    }

    private void filterTickets(String status) {
        displayedList.clear();
        for (TicketModel ticket : fullTicketList) {
            if (ticket.getStatus().equals(status)) {
                displayedList.add(ticket);
            }
        }
        ticketAdapter.notifyDataSetChanged();
    }

    private void setupTabListeners() {
        tabBooked.setOnClickListener(v -> {
            updateTabUI(tabBooked, tabCompleted, tabCancelled);
            tvListTitle.setText("Danh sách vé đã đặt");
            filterTickets("Booked");
        });

        tabCompleted.setOnClickListener(v -> {
            updateTabUI(tabCompleted, tabBooked, tabCancelled);
            tvListTitle.setText("Danh sách vé đã đi");
            filterTickets("Completed");
        });

        tabCancelled.setOnClickListener(v -> {
            updateTabUI(tabCancelled, tabBooked, tabCompleted);
            tvListTitle.setText("Danh sách vé đã hủy");
            filterTickets("Cancelled");
        });
    }

    private void updateTabUI(TextView selected, TextView o1, TextView o2) {
        selected.setTextColor(Color.BLACK);
        selected.setTypeface(null, Typeface.BOLD);
        selected.setBackgroundResource(R.drawable.tab_selected_border);

        o1.setTextColor(Color.GRAY);
        o1.setTypeface(null, Typeface.NORMAL);
        o1.setBackground(null);

        o2.setTextColor(Color.GRAY);
        o2.setTypeface(null, Typeface.NORMAL);
        o2.setBackground(null);
    }

    private void setupNavigationListeners() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navFeedback = findViewById(R.id.nav_feedback);
        ImageView btnProfile = findViewById(R.id.btnProfileHeader);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }

        if (navFeedback != null) {
            navFeedback.setOnClickListener(v -> {
                startActivity(new Intent(this, PhanHoiActivity.class));
                finish();
            });
        }
        
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, CustomerProfileActivity.class));
            });
        }
    }

    @Override
    public void onTicketClick(TicketModel ticket) {
        showTicketDetailDialog(ticket);
    }

    private void showTicketDetailDialog(TicketModel ticket) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_ticket_detail);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Cập nhật dữ liệu vào Dialog
        TextView tvDate = dialog.findViewById(R.id.tvDate);
        TextView tvTime = dialog.findViewById(R.id.tvTime);
        TextView tvSeats = dialog.findViewById(R.id.tvSeatNumber);
        TextView tvStatus = dialog.findViewById(R.id.tvStatusDetail);
        
        if (tvDate != null) tvDate.setText("Ngày khởi hành: " + ticket.getDate());
        if (tvTime != null) tvTime.setText("Giờ khởi hành: " + ticket.getTime());
        if (tvSeats != null) tvSeats.setText("Mã số ghế: " + ticket.getSeats());
        
        if (tvStatus != null) {
            String statusText = "";
            if (ticket.getStatus().equals("Booked")) statusText = "Trạng thái: Đã đặt";
            else if (ticket.getStatus().equals("Completed")) statusText = "Trạng thái: Đã đi";
            else if (ticket.getStatus().equals("Cancelled")) statusText = "Trạng thái: Đã hủy";
            tvStatus.setText(statusText);
        }

        ImageView btnClose = dialog.findViewById(R.id.btnClose);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
