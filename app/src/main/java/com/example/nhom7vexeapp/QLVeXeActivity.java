package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.adapters.TicketAdapter;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Ticket;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QLVeXeActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {

    private RecyclerView rvTickets;
    private TicketAdapter adapter;
    private List<Ticket> currentDisplayList;
    
    private TextView tabBooked, tabCompleted, tabCancelled, tvListTitle;
    private LinearLayout navHome, navSearch, navTickets, navFeedback;
    private ProgressBar progressBar;
    private TextView tvEmptyMessage;
    
    private SharedPreferences sharedPreferences;
    private ApiService apiService;
    private String customerUid;
    private String loggedInCustomerName = "Khách hàng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_ve_xe);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        customerUid = sharedPreferences.getString("customerUid", "");
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupRecyclerView();
        setupTabEvents();
        setupBottomNavigation();
        
        if (customerUid.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loggedInCustomerName = sharedPreferences.getString("customerName", "Khách hàng");
        fetchCustomerName(customerUid);
        switchTab("Đã đặt");
    }

    private void initViews() {
        rvTickets = findViewById(R.id.rvTickets);
        tvListTitle = findViewById(R.id.tvListTitle);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        
        tabBooked = findViewById(R.id.tab_booked);
        tabCompleted = findViewById(R.id.tab_completed);
        tabCancelled = findViewById(R.id.tab_cancelled);
        
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets_btn);
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void setupRecyclerView() {
        currentDisplayList = new ArrayList<>();
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(this, currentDisplayList, this);
        rvTickets.setAdapter(adapter);
    }

    private void setupTabEvents() {
        tabBooked.setOnClickListener(v -> switchTab("Đã đặt"));
        tabCompleted.setOnClickListener(v -> switchTab("Đã đi"));
        tabCancelled.setOnClickListener(v -> switchTab("Đã hủy"));
    }

    private void switchTab(String status) {
        updateTabUI(status);
        fetchTickets(status);
    }

    private void updateTabUI(String status) {
        tabBooked.setTextColor(Color.parseColor("#888888"));
        tabBooked.setBackground(null);
        tabCompleted.setTextColor(Color.parseColor("#888888"));
        tabCompleted.setBackground(null);
        tabCancelled.setTextColor(Color.parseColor("#888888"));
        tabCancelled.setBackground(null);

        if (status.equals("Đã đặt")) {
            tvListTitle.setText("Danh sách vé đã đặt");
            tabBooked.setTextColor(Color.BLACK);
            tabBooked.setBackgroundResource(R.drawable.tab_selected_border);
        } else if (status.equals("Đã đi")) {
            tvListTitle.setText("Danh sách vé đã đi");
            tabCompleted.setTextColor(Color.BLACK);
            tabCompleted.setBackgroundResource(R.drawable.tab_selected_border);
        } else {
            tvListTitle.setText("Danh sách vé đã hủy");
            tabCancelled.setTextColor(Color.BLACK);
            tabCancelled.setBackgroundResource(R.drawable.tab_selected_border);
        }
    }

    private void fetchTickets(String status) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvEmptyMessage != null) tvEmptyMessage.setVisibility(View.GONE);
        
        apiService.getTickets(customerUid, status).enqueue(new Callback<List<Ticket>>() {
            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentDisplayList.clear();
                    String myKhId = customerUid != null ? customerUid.trim() : "";
                    for (Ticket t : response.body()) {
                        String ticketOwner = t.getOwnerId() != null ? t.getOwnerId().trim() : "";
                        if (status.equalsIgnoreCase(t.getTrangThai()) && ticketOwner.equalsIgnoreCase(myKhId)) {
                            currentDisplayList.add(t);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (currentDisplayList.isEmpty()) {
                        if (tvEmptyMessage != null) {
                            tvEmptyMessage.setText("Bạn chưa có đơn đặt vé nào.");
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    showErrorDialog("Không thể tải danh sách đặt vé. Vui lòng thử lại sau.");
                }
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                showErrorDialog("Lỗi kết nối server! Vui lòng kiểm tra internet.");
            }
        });
    }

    private void fetchCustomerName(String uid) {
        apiService.getKhachHangDetail(uid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    String name = findValue(data, "Hovaten", "TenKhachHang", "HoTen");
                    if (!name.isEmpty()) {
                        loggedInCustomerName = name;
                        sharedPreferences.edit().putString("customerName", loggedInCustomerName).apply();
                    }
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private String findValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                String val = map.get(key).toString();
                if (!val.equalsIgnoreCase("null") && !val.isEmpty()) return val;
            }
        }
        return "";
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage(message)
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    String currentStatus = "Đã đặt";
                    String title = tvListTitle.getText().toString().toLowerCase();
                    if (title.contains("đã đi")) currentStatus = "Đã đi";
                    else if (title.contains("đã hủy")) currentStatus = "Đã hủy";
                    fetchTickets(currentStatus);
                    fetchCustomerName(customerUid);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    @Override
    public void onTicketClick(Ticket ticket) {
        showTicketDetailDialog(ticket);
    }

    @Override
    public void onCancelClick(Ticket ticket) {
        showCancelConfirmDialog(ticket);
    }

    private void showCancelConfirmDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_booking, null);
        
        Button btnNo = view.findViewById(R.id.btnCancelNo);
        Button btnYes = view.findViewById(R.id.btnCancelYes);

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            if ("Đã thanh toán".equalsIgnoreCase(ticket.getTrangThaiThanhToan())) {
                showRefundInfoDialog(ticket);
            } else {
                deleteTicket(ticket, false);
            }
        });
        
        dialog.show();
    }

    private void showRefundInfoDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_refund_info, null);
        
        EditText etName = view.findViewById(R.id.etRefundName);
        EditText etCard = view.findViewById(R.id.etRefundCardNumber);
        EditText etBank = view.findViewById(R.id.etRefundBank);
        Button btnCancel = view.findViewById(R.id.btnRefundCancel);
        Button btnConfirm = view.findViewById(R.id.btnRefundConfirm);

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String card = etCard.getText().toString().trim();
            String bank = etBank.getText().toString().trim();
            
            if (name.isEmpty() || card.isEmpty() || bank.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            
            dialog.dismiss();
            deleteTicket(ticket, true);
        });
        
        dialog.show();
    }

    private void deleteTicket(Ticket ticket, boolean isRefund) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        apiService.deleteTicket(ticket.getVeID()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    if (isRefund) {
                        showRefundSuccessDialog();
                    } else {
                        showCancelSuccessDialog();
                    }
                    fetchTickets("Đã đặt");
                } else {
                    showCancelFailedDialog();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(QLVeXeActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_success, null);
        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        new android.os.Handler().postDelayed(dialog::dismiss, 2000);
    }

    private void showRefundSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_refund_success, null);
        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        new android.os.Handler().postDelayed(dialog::dismiss, 3000);
    }

    private void showCancelFailedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_failed, null);
        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
        new android.os.Handler().postDelayed(dialog::dismiss, 2500);
    }

    private void showTicketDetailDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ticket_detail, null);

        ImageView btnClose = view.findViewById(R.id.btnClose);
        TextView tvTicketCode = view.findViewById(R.id.tvTicketCode);
        TextView tvCustomerName = view.findViewById(R.id.tvCustomerName);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvSeatCount = view.findViewById(R.id.tvSeatCount);
        TextView tvSeatNumber = view.findViewById(R.id.tvSeatNumber);
        TextView tvStatusDetail = view.findViewById(R.id.tvStatusDetail);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvTotal = view.findViewById(R.id.tvTotal);
        Button btnPayment = view.findViewById(R.id.btnPayment);

        tvTicketCode.setText(ticket.getVeID());
        tvCustomerName.setText("Khách hàng: " + loggedInCustomerName);
        tvDate.setText("Ngày khởi hành: " + ticket.getNgayKhoiHanh());
        tvTime.setText("Giờ khởi hành: " + ticket.getGioDi());
        tvSeatCount.setText("Số lượng ghế: " + String.format("%02d", ticket.getSoLuongGhe()));
        tvSeatNumber.setText("Mã số ghế: " + ticket.getFormattedSeats());
        tvStatusDetail.setText("Trạng thái: " + ticket.getTrangThai());

        DecimalFormat df = new DecimalFormat("#,###");
        String formattedTotal = df.format(ticket.getTongTien()) + " vnđ";
        double pricePerSeat = ticket.getSoLuongGhe() > 0 ? ticket.getTongTien() / ticket.getSoLuongGhe() : 0;
        tvPrice.setText(df.format(pricePerSeat) + " vnđ");
        tvTotal.setText(formattedTotal);

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if ("Đã hủy".equalsIgnoreCase(ticket.getTrangThai())) {
            btnPayment.setVisibility(View.VISIBLE);
            btnPayment.setText("Đã hủy");
            btnPayment.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#33B5E5"))); 
            btnPayment.setEnabled(false);
        } else if ("Đã đi".equalsIgnoreCase(ticket.getTrangThai())) {
            btnPayment.setVisibility(View.VISIBLE);
            btnPayment.setText("Đã thanh toán");
            btnPayment.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFC107"))); 
            btnPayment.setEnabled(false);
        } else {
            if ("Chưa thanh toán".equalsIgnoreCase(ticket.getTrangThaiThanhToan())) {
                btnPayment.setVisibility(View.VISIBLE);
                btnPayment.setText("Thanh toán");
                btnPayment.setEnabled(true);
                btnPayment.setOnClickListener(v -> {
                    dialog.dismiss();
                    showPaymentDialog(ticket);
                });
            } else {
                btnPayment.setVisibility(View.VISIBLE);
                btnPayment.setText("Đã thanh toán");
                btnPayment.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
                btnPayment.setEnabled(false);
            }
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPaymentDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null);

        ImageView btnClose = view.findViewById(R.id.btnPaymentClose);
        TextView tvCode = view.findViewById(R.id.tvPayTicketCode);
        TextView tvRoute = view.findViewById(R.id.tvPayRoute);
        TextView tvDate = view.findViewById(R.id.tvPayDate);
        TextView tvTime = view.findViewById(R.id.tvPayTime);
        TextView tvSeats = view.findViewById(R.id.tvPaySeats);
        TextView tvTotal = view.findViewById(R.id.tvPayTotal);
        
        MaterialCardView cardBank = view.findViewById(R.id.cardBankTransfer);
        MaterialCardView cardCash = view.findViewById(R.id.cardCash);
        MaterialCardView iconBankCont = view.findViewById(R.id.iconBankContainer);
        MaterialCardView iconCashCont = view.findViewById(R.id.iconCashContainer);
        RadioButton rbBank = view.findViewById(R.id.rbBankTransfer);
        RadioButton rbCash = view.findViewById(R.id.rbCash);
        
        MaterialCardView cardNote = view.findViewById(R.id.cardNote);
        Button btnCancel = view.findViewById(R.id.btnPayCancel);
        Button btnConfirm = view.findViewById(R.id.btnPayConfirm);

        tvCode.setText(ticket.getVeID());
        tvRoute.setText(ticket.getTenTuyen());
        tvDate.setText(ticket.getNgayKhoiHanh());
        tvTime.setText(ticket.getGioDi());
        tvSeats.setText(ticket.getFormattedSeats());
        
        DecimalFormat df = new DecimalFormat("#,###");
        tvTotal.setText(df.format(ticket.getTongTien()) + " đ");

        final String lightBlue = "#F0F7FF";
        final String darkBlue = "#0047AB";
        final String softBlueIcon = "#BBDDFF"; 
        final String grayBorder = "#EEEEEE";
        final String grayBg = "#F5F5F5";

        Runnable updateUI = () -> {
            boolean isCash = rbCash.isChecked();
            cardCash.setCardBackgroundColor(isCash ? Color.parseColor(lightBlue) : Color.WHITE);
            cardCash.setStrokeColor(isCash ? Color.parseColor(darkBlue) : Color.parseColor(grayBorder));
            iconCashCont.setCardBackgroundColor(isCash ? Color.parseColor(softBlueIcon) : Color.parseColor(grayBg));
            cardBank.setCardBackgroundColor(!isCash ? Color.parseColor(lightBlue) : Color.WHITE);
            cardBank.setStrokeColor(!isCash ? Color.parseColor(darkBlue) : Color.parseColor(grayBorder));
            iconBankCont.setCardBackgroundColor(!isCash ? Color.parseColor(softBlueIcon) : Color.parseColor(grayBg));
            cardNote.setVisibility(isCash ? View.VISIBLE : View.GONE);
        };

        rbCash.setChecked(true);
        updateUI.run();

        View.OnClickListener bankClick = v -> { rbBank.setChecked(true); rbCash.setChecked(false); updateUI.run(); };
        View.OnClickListener cashClick = v -> { rbCash.setChecked(true); rbBank.setChecked(false); updateUI.run(); };

        cardBank.setOnClickListener(bankClick);
        cardCash.setOnClickListener(cashClick);

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (rbCash.isChecked()) {
                showPaymentSuccessDialog("Vui lòng chuẩn bị tiền khi lên xe");
            } else {
                showFeatureUpcomingDialog();
            }
        });

        dialog.show();
    }

    private void showFeatureUpcomingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_feature_upcoming, null);
        Button btnOk = view.findViewById(R.id.btnUpcomingOk);

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            // Điều hướng quay lại trang quản lý vé (làm mới lại tab hiện tại)
            switchTab("Đã đặt");
        });

        dialog.show();
    }

    private void showQrPaymentDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_qr_payment, null);

        ImageView btnClose = view.findViewById(R.id.btnQrClose);
        TextView tvCode = view.findViewById(R.id.tvQrTicketCode);
        TextView tvRoute = view.findViewById(R.id.tvQrRoute);
        TextView tvDate = view.findViewById(R.id.tvQrDate);
        TextView tvTime = view.findViewById(R.id.tvQrTime);
        TextView tvSeats = view.findViewById(R.id.tvQrSeats);
        TextView tvTotal = view.findViewById(R.id.tvQrTotal);
        Button btnCancel = view.findViewById(R.id.btnQrCancel);

        tvCode.setText(ticket.getVeID());
        tvRoute.setText(ticket.getTenTuyen());
        tvDate.setText(ticket.getNgayKhoiHanh());
        tvTime.setText(ticket.getGioDi());
        tvSeats.setText(ticket.getFormattedSeats());
        
        DecimalFormat df = new DecimalFormat("#,###");
        tvTotal.setText(df.format(ticket.getTongTien()) + " đ");

        AlertDialog dialog = builder.setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showPaymentSuccessDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_payment_success, null);
        TextView tvMsg = view.findViewById(R.id.tvPaymentSuccessMessage);
        tvMsg.setText(message);

        AlertDialog successDialog = builder.setView(view).create();
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        successDialog.show();

        new android.os.Handler().postDelayed(() -> {
            if (successDialog.isShowing()) {
                successDialog.dismiss();
            }
        }, 2000);
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
