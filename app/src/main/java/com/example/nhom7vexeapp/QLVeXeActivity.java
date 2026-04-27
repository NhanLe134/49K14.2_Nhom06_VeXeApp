package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.adapters.TicketAdapter;
import com.example.nhom7vexeapp.models.Ticket;
import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
        if (customerId.isEmpty()) {
            customerId = sharedPreferences.getString("user_id", "");
        }

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
        if (customerId == null || customerId.isEmpty()) {
            tvEmptyMessage.setText("Vui lòng đăng nhập để xem vé");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            ticketList.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyMessage.setVisibility(View.GONE);
        ticketList.clear();

        if ("Đã hủy".equalsIgnoreCase(currentStatus)) {
            loadAllCancelledTickets();
        } else {
            apiService.getTickets(customerId, currentStatus).enqueue(new Callback<List<Ticket>>() {
                @Override
                public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        for (Ticket ticket : response.body()) {
                            if (currentStatus.equalsIgnoreCase(ticket.getTrangThai()) && 
                                customerId.equals(ticket.getKhachHangID())) {
                                ticketList.add(ticket);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyMessage();
                    }
                }

                @Override
                public void onFailure(Call<List<Ticket>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(QLVeXeActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadAllCancelledTickets() {
        apiService.getVeHuy(customerId).enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ticketList.addAll(response.body());
                }
                fetchCancelledFromMainTable();
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                fetchCancelledFromMainTable();
            }
        });
    }

    private void fetchCancelledFromMainTable() {
        apiService.getTickets(customerId, "Đã hủy").enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    for (Ticket t : response.body()) {
                        boolean exists = false;
                        for (Ticket existing : ticketList) {
                            if (t.getVeID() != null && t.getVeID().equals(existing.getVeID())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists && "Đã hủy".equalsIgnoreCase(t.getTrangThai())) {
                            ticketList.add(t);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                updateEmptyMessage();
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                updateEmptyMessage();
            }
        });
    }

    private void updateEmptyMessage() {
        if (ticketList.isEmpty()) {
            tvEmptyMessage.setText("Không có vé nào trong mục " + currentStatus);
            tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
        }
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
    public void onTicketClick(Ticket ticket) {
        if (ticket.getVeID() == null) return;
        showTicketDetailDialog(ticket);
    }

    private void showTicketDetailDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ticket_detail, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTicketCode = dialogView.findViewById(R.id.tvTicketCode);
        TextView tvCustomerName = dialogView.findViewById(R.id.tvCustomerName);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvTime = dialogView.findViewById(R.id.tvTime);
        TextView tvSeatCount = dialogView.findViewById(R.id.tvSeatCount);
        TextView tvSeatNumber = dialogView.findViewById(R.id.tvSeatNumber);
        TextView tvStatusDetail = dialogView.findViewById(R.id.tvStatusDetail);
        TextView tvPrice = dialogView.findViewById(R.id.tvPrice);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotal);
        ImageView btnClose = dialogView.findViewById(R.id.btnClose);
        Button btnPayment = dialogView.findViewById(R.id.btnPayment);

        tvTicketCode.setText(ticket.getVeID());
        String name = sharedPreferences.getString("customerName", "Khách hàng");
        tvCustomerName.setText("Khách hàng: " + name);
        tvDate.setText("Ngày khởi hành: " + ticket.getNgayKhoiHanh());
        tvTime.setText("Giờ khởi hành: " + ticket.getGioDi());
        tvSeatCount.setText("Số lượng ghế: " + String.format("%02d", ticket.getSoLuongGhe()));
        tvSeatNumber.setText("Mã số ghế: " + ticket.getFormattedSeats());
        tvStatusDetail.setText("Trạng thái: " + ticket.getTrangThai());

        DecimalFormat formatter = new DecimalFormat("#,###");
        double singlePrice = ticket.getSoLuongGhe() > 0 ? ticket.getTongTien() / ticket.getSoLuongGhe() : 0;
        tvPrice.setText(formatter.format(singlePrice) + " vnđ");
        tvTotal.setText(formatter.format(ticket.getTongTien()) + " vnđ");

        String status = (ticket.getTrangThai() != null) ? ticket.getTrangThai().trim() : "";
        String paymentStatus = (ticket.getTrangThaiThanhToan() != null) ? ticket.getTrangThaiThanhToan().trim() : "";

        btnPayment.setVisibility(View.VISIBLE);
        btnPayment.setEnabled(false);
        btnPayment.setTextColor(Color.WHITE);

        if ("Đã đặt".equalsIgnoreCase(status)) {
            if ("Đã thanh toán".equalsIgnoreCase(paymentStatus)) {
                btnPayment.setText("Đã thanh toán");
                btnPayment.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F1C40F")));
            } else {
                btnPayment.setText("Thanh toán");
                btnPayment.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#34B5F1")));
                btnPayment.setEnabled(true);
                btnPayment.setOnClickListener(v -> {
                    dialog.dismiss();
                    showPaymentDialog(ticket);
                });
            }
        } else if ("Đã đi".equalsIgnoreCase(status)) {
            btnPayment.setText("Đã thanh toán");
            btnPayment.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F1C40F")));
        } else if ("Đã hủy".equalsIgnoreCase(status)) {
            btnPayment.setText("Đã hủy");
            btnPayment.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#34B5F1")));
        } else {
            btnPayment.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPaymentDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvPayTotal = dialogView.findViewById(R.id.tvPayTotal);
        MaterialCardView cardNote = dialogView.findViewById(R.id.cardNote);
        MaterialCardView cardBank = dialogView.findViewById(R.id.cardBankTransfer);
        MaterialCardView cardCash = dialogView.findViewById(R.id.cardCash);
        ImageView imgBank = dialogView.findViewById(R.id.imgBank);
        ImageView imgCash = dialogView.findViewById(R.id.imgCash);
        MaterialCardView iconBankContainer = dialogView.findViewById(R.id.iconBankContainer);
        MaterialCardView iconCashContainer = dialogView.findViewById(R.id.iconCashContainer);
        Button btnPayConfirm = dialogView.findViewById(R.id.btnPayConfirm);
        Button btnPayCancel = dialogView.findViewById(R.id.btnPayCancel);

        ((TextView)dialogView.findViewById(R.id.tvPayTicketCode)).setText(ticket.getVeID());
        ((TextView)dialogView.findViewById(R.id.tvPayRoute)).setText(ticket.getTenTuyen());
        ((TextView)dialogView.findViewById(R.id.tvPayDate)).setText(ticket.getNgayKhoiHanh());
        ((TextView)dialogView.findViewById(R.id.tvPayTime)).setText(ticket.getGioDi());
        ((TextView)dialogView.findViewById(R.id.tvPaySeats)).setText(ticket.getFormattedSeats());
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvPayTotal.setText(formatter.format(ticket.getTongTien()) + " đ");
        tvPayTotal.setTextColor(Color.parseColor("#2563EB"));

        int activeBlue = Color.parseColor("#3B82F6");
        int activeBg = Color.parseColor("#EEF2FF");
        int blackBtn = Color.parseColor("#1A1C2E");
        int grayBtn = Color.parseColor("#F5F5F5");
        int grayBorder = Color.parseColor("#EEEEEE");
        int grayIconBg = Color.parseColor("#F5F5F5");

        btnPayConfirm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blackBtn));
        btnPayConfirm.setTextColor(Color.WHITE);
        btnPayCancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(grayBtn));
        btnPayCancel.setTextColor(Color.BLACK);

        final String[] paymentMethod = {"Tiền mặt"};

        Runnable updateUI = () -> {
            if ("Tiền mặt".equals(paymentMethod[0])) {
                cardCash.setStrokeColor(activeBlue);
                cardCash.setCardBackgroundColor(activeBg);
                iconCashContainer.setCardBackgroundColor(activeBlue);
                imgCash.setColorFilter(Color.WHITE);
                cardBank.setStrokeColor(grayBorder);
                cardBank.setCardBackgroundColor(Color.WHITE);
                iconBankContainer.setCardBackgroundColor(grayIconBg);
                imgBank.setColorFilter(Color.BLACK);
                cardNote.setVisibility(View.VISIBLE);
            } else {
                cardBank.setStrokeColor(activeBlue);
                cardBank.setCardBackgroundColor(activeBg);
                iconBankContainer.setCardBackgroundColor(activeBlue);
                imgBank.setColorFilter(Color.WHITE);
                cardCash.setStrokeColor(grayBorder);
                cardCash.setCardBackgroundColor(Color.WHITE);
                iconCashContainer.setCardBackgroundColor(grayIconBg);
                imgCash.setColorFilter(Color.BLACK);
                cardNote.setVisibility(View.GONE);
            }
        };

        updateUI.run();
        cardBank.setOnClickListener(v -> { paymentMethod[0] = "Chuyển khoản"; updateUI.run(); });
        cardCash.setOnClickListener(v -> { paymentMethod[0] = "Tiền mặt"; updateUI.run(); });

        btnPayConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if ("Chuyển khoản".equals(paymentMethod[0])) {
                showUpcomingDialog();
            } else {
                showSuccessDialog();
            }
        });

        btnPayCancel.setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnPaymentClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showUpcomingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_feature_upcoming, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialogView.findViewById(R.id.btnUpcomingOk).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment_success, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        dialogView.postDelayed(dialog::dismiss, 2500);
    }

    @Override
    public void onCancelClick(Ticket ticket) {
        // Logic hủy vé sẽ được triển khai sau
    }
}
