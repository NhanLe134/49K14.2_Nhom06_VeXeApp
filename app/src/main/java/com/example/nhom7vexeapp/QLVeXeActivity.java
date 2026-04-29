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
import com.example.nhom7vexeapp.models.KhachHang;
import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.Route;
import com.example.nhom7vexeapp.models.Ticket;
import com.example.nhom7vexeapp.models.Trip;
import com.example.nhom7vexeapp.models.TripSearchResult;
import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
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
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Ticket> cancelledTickets = response.body();

                    // Cập nhật danh sách một lần duy nhất ở đây để tránh lặp và giật lag
                    ticketList.clear();
                    ticketList.addAll(cancelledTickets);
                    adapter.notifyDataSetChanged();
                    updateEmptyMessage();

                    for (int i = 0; i < cancelledTickets.size(); i++) {
                        final int index = i;
                        Ticket ticket = cancelledTickets.get(i);
                        if (ticket.getChuyenXeID() != null) {
                            apiService.getTripDetail(ticket.getChuyenXeID()).enqueue(new Callback<Trip>() {
                                @Override
                                public void onResponse(Call<Trip> call, Response<Trip> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Trip trip = response.body();
                                        ticket.setTenTuyen(trip.getRouteName());
                                        ticket.setTenNhaXe(trip.getTenNhaXe());
                                        ticket.setNgayKhoiHanh(trip.getDate());
                                        ticket.setGioDi(trip.getTime());

                                        // Chỉ cập nhật đúng ô vé vừa lấy được thông tin
                                        adapter.notifyItemChanged(index);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Trip> call, Throwable t) {
                                    Log.e("ENRICH_ERROR", "Không lấy được chi tiết chuyến xe: " + t.getMessage());
                                }
                            });
                        }
                    }
                } else {
                    updateEmptyMessage();
                }
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                updateEmptyMessage();
                Toast.makeText(QLVeXeActivity.this, "Lỗi tải danh sách vé hủy", Toast.LENGTH_SHORT).show();
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
        if (ticket.getVeID() == null)
            return;
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
        TextView tvRouteDetail = dialogView.findViewById(R.id.tvRouteDetail);
        TextView tvCustomerName = dialogView.findViewById(R.id.tvCustomerName);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvTime = dialogView.findViewById(R.id.tvTime);
        TextView tvSeatCount = dialogView.findViewById(R.id.tvSeatCount);
        TextView tvSeatNumber = dialogView.findViewById(R.id.tvSeatNumber);
        TextView tvOperator = dialogView.findViewById(R.id.tvOperator);
        TextView tvVehicle = dialogView.findViewById(R.id.tvVehicle);
        TextView tvStatusDetail = dialogView.findViewById(R.id.tvStatusDetail);
        TextView tvPrice = dialogView.findViewById(R.id.tvPrice);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotal);
        ImageView btnClose = dialogView.findViewById(R.id.btnClose);
        Button btnPayment = dialogView.findViewById(R.id.btnPayment);

        // Hiển thị thông tin cơ bản trước
        tvTicketCode.setText(ticket.getVeID());
        tvRouteDetail.setText("Tuyến: " + (ticket.getTenTuyen() != null ? ticket.getTenTuyen() : "Đang tải..."));
        tvCustomerName.setText("Khách hàng: Đang tải...");
        tvOperator.setText("Nhà xe: " + (ticket.getTenNhaXe() != null ? ticket.getTenNhaXe() : "Đang tải..."));
        tvVehicle.setText("Loại xe: Đang tải...");
        tvDate.setText("Ngày khởi hành: " + ticket.getNgayKhoiHanh());
        tvTime.setText("Giờ khởi hành: " + ticket.getGioDi());
        tvSeatCount.setText("Số lượng ghế: " + String.format("%02d", ticket.getSoLuongGhe()));
        tvSeatNumber.setText("Mã số ghế: " + ticket.getFormattedSeats());
        tvStatusDetail.setText("Trạng thái: " + ticket.getTrangThai());

        DecimalFormat formatter = new DecimalFormat("#,###");
        double singlePrice = ticket.getSoLuongGhe() > 0 ? ticket.getTongTien() / ticket.getSoLuongGhe() : 0;
        tvPrice.setText(formatter.format(singlePrice) + " vnđ");
        tvTotal.setText(formatter.format(ticket.getTongTien()) + " vnđ");

        // TRUY VẤN NÂNG CAO ĐỂ LẤY DỮ LIỆU CHÍNH XÁC
        fetchEnrichedData(ticket, tvCustomerName, tvRouteDetail, tvDate, tvTime, tvOperator, tvVehicle);

        String status = (ticket.getTrangThai() != null) ? ticket.getTrangThai().trim() : "";
        String paymentStatus = (ticket.getTrangThaiThanhToan() != null) ? ticket.getTrangThaiThanhToan().trim() : "";

        btnPayment.setVisibility(View.VISIBLE);
        btnPayment.setEnabled(false);
        btnPayment.setTextColor(Color.WHITE);

        if ("Đã đặt".equalsIgnoreCase(status)) {
            if ("Đã thanh toán".equalsIgnoreCase(paymentStatus)) {
                btnPayment.setText("Đã thanh toán");
                btnPayment
                        .setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F1C40F")));
            } else {
                btnPayment.setText("Thanh toán");
                btnPayment
                        .setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#34B5F1")));
                btnPayment.setEnabled(true);
                btnPayment.setOnClickListener(v -> {
                    dialog.dismiss();
                    showUpcomingDialog();
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

    @Override
    public void onCancelClick(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cancel_booking, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnCancelNo).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnCancelYes).setOnClickListener(v -> {
            dialog.dismiss();
            processCancelTicket(ticket);
        });
        dialog.show();
    }

    private void processCancelTicket(Ticket ticket) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> data = new HashMap<>();
        data.put("ve_id", ticket.getVeID());

        Log.d("HUY_VE_LOG", "Gửi ve_id: " + ticket.getVeID());

        apiService.createVeHuy(data).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(QLVeXeActivity.this, "Hủy vé thành công!", Toast.LENGTH_SHORT).show();
                    loadTickets(); // Reload để thấy vé mới trong tab "Đã hủy"
                } else {
                    try {
                        String errorMsg = response.errorBody() != null ? response.errorBody().string()
                                : "Unknown error";
                        Log.e("HUY_VE_LOG", "Lỗi: " + response.code() + " - " + errorMsg);
                        Toast.makeText(QLVeXeActivity.this, "Lỗi Server: " + errorMsg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("HUY_VE_LOG", "Failure: ", t);
                Toast.makeText(QLVeXeActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEnrichedData(Ticket ticket, TextView tvCust, TextView tvRoute, TextView tvDate, TextView tvTime,
            TextView tvOp, TextView tvVeh) {
        // 1. Lấy tên khách hàng từ ID
        if (ticket.getKhachHangID() != null) {
            apiService.getKhachHangDetail(ticket.getKhachHangID()).enqueue(new Callback<KhachHang>() {
                @Override
                public void onResponse(Call<KhachHang> call, Response<KhachHang> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String name = response.body().getHoTen();
                        if (name != null && !name.isEmpty()) {
                            tvCust.setText("Khách hàng: " + name);
                        } else {
                            // Fallback: nếu tên null thì hiện Email hoặc ID
                            String email = response.body().getEmail();
                            tvCust.setText("Khách hàng: " + (email != null ? email : ticket.getKhachHangID()));
                        }
                    }
                }

                @Override
                public void onFailure(Call<KhachHang> call, Throwable t) {
                }
            });
        }

        // 2. Lấy thông tin chi tiết từ chuyến xe
        if (ticket.getChuyenXeID() != null) {
            apiService.getTripDetail(ticket.getChuyenXeID()).enqueue(new Callback<Trip>() {
                @Override
                public void onResponse(Call<Trip> call, Response<Trip> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Trip trip = response.body();
                        // Cập nhật thông tin từ chuyến xe
                        tvDate.setText("Ngày khởi hành: " + trip.getDate());
                        tvTime.setText("Giờ khởi hành: " + trip.getTime());
                        tvOp.setText("Nhà xe: " + trip.getTenNhaXe());

                        // Xử lý loại xe
                        String carType = trip.getLoaiXe();
                        if (carType != null && carType.startsWith("LX")) {
                            fetchVehicleType(carType, tvVeh);
                        } else {
                            tvVeh.setText("Loại xe: " + trip.getVehicleType());
                        }

                        // Xử lý tên tuyến
                        String routeName = trip.getRouteName();
                        if (routeName == null || routeName.isEmpty() || routeName.contains(trip.getTuyenXeID())) {
                            fetchRouteName(trip.getTuyenXeID(), tvRoute);
                        } else {
                            tvRoute.setText("Tuyến: " + routeName);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Trip> call, Throwable t) {
                }
            });
        }
    }

    private void fetchVehicleType(String loaixeId, TextView tvVeh) {
        if (loaixeId == null || loaixeId.isEmpty())
            return;
        apiService.getLoaixe().enqueue(new Callback<List<Loaixe>>() {
            @Override
            public void onResponse(Call<List<Loaixe>> call, Response<List<Loaixe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Loaixe lx : response.body()) {
                        if (loaixeId.equals(lx.getLoaixeID())) {
                            tvVeh.setText("Loại xe: Xe " + lx.getSoCho() + " chỗ");
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Loaixe>> call, Throwable t) {
            }
        });
    }

    private void fetchRouteName(String routeId, TextView tvRoute) {
        if (routeId == null || routeId.isEmpty() || routeId.equalsIgnoreCase("null"))
            return;
        apiService.getRoutes().enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Route route : response.body()) {
                        if (routeId.equals(route.getId())) {
                            tvRoute.setText("Tuyến: " + route.getName());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
            }
        });
    }
}
