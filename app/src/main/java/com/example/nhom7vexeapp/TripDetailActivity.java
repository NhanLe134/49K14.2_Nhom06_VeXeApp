package com.example.nhom7vexeapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.adapters.PassengerAdapter;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Driver;
import com.example.nhom7vexeapp.models.Passenger;
import com.example.nhom7vexeapp.models.Trip;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripDetailActivity extends AppCompatActivity {

    private TextView tvRouteName, tvTime, tvDuration, tvTotalSeats, tvAvailableSeats, tvStatus, tvStatusOption;
    private MaterialButton btnAssign;
    private RecyclerView rvPassengers;
    private Trip trip;
    private List<Passenger> passengerList = new ArrayList<>();
    private PassengerAdapter adapter;
    private ImageView imgOpProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        initViews();
        loadData();
        setupBottomNavigation();
    }

    private void initViews() {
        tvRouteName = findViewById(R.id.tvRouteName);
        tvTime = findViewById(R.id.tvTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvTotalSeats = findViewById(R.id.tvTotalSeats);
        tvAvailableSeats = findViewById(R.id.tvAvailableSeats);
        tvStatus = findViewById(R.id.tvStatus);
        tvStatusOption = findViewById(R.id.tvStatusOption);
        btnAssign = findViewById(R.id.btnAssign);
        rvPassengers = findViewById(R.id.rvPassengers);
        imgOpProfile = findViewById(R.id.imgOpProfile);
        
        rvPassengers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PassengerAdapter(passengerList);
        rvPassengers.setAdapter(adapter);

        // ✅ LINK ĐẾN MÀN HÌNH PROFILE NHÀ XE
        if (imgOpProfile != null) {
            imgOpProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorProfileActivity.class));
            });
        }

        tvStatus.setOnClickListener(v -> {
            if (tvStatusOption.getVisibility() == View.VISIBLE) {
                tvStatusOption.setVisibility(View.GONE);
            } else {
                String optionText = trip.getStatus().equals("Hoàn thành") ? "Chưa hoàn thành" : "Hoàn thành";
                tvStatusOption.setText(optionText);
                tvStatusOption.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        optionText.equals("Hoàn thành") ? Color.parseColor("#E8F5E9") : Color.parseColor("#FFF9C4")));
                tvStatusOption.setTextColor(optionText.equals("Hoàn thành") ? Color.parseColor("#4CAF50") : Color.parseColor("#FBC02D"));
                tvStatusOption.setVisibility(View.VISIBLE);
            }
        });

        tvStatusOption.setOnClickListener(v -> {
            tvStatusOption.setVisibility(View.GONE);
            showConfirmStatusDialog(tvStatusOption.getText().toString());
        });
    }

    private void showConfirmStatusDialog(String newStatus) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_status);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            updateTripStatusOnServer(newStatus);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateTripStatusOnServer(String newStatus) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, Object> data = new HashMap<>();
        data.put("TrangThai", newStatus);

        apiService.patchTrip(trip.getId(), data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    trip.setStatus(newStatus);
                    updateStatusUI(newStatus);
                    if (newStatus.equals("Hoàn thành")) {
                        updateAllTicketsToPendingReview();
                    }
                    Toast.makeText(TripDetailActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TripDetailActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TripDetailActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatusUI(String status) {
        tvStatus.setText(status);
        if (status.equals("Hoàn thành")) {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF9C4")));
            tvStatus.setTextColor(Color.parseColor("#FBC02D"));
        }
        tvStatusOption.setVisibility(View.GONE);
    }

    private void updateAllTicketsToPendingReview() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getTicketsByTrip(trip.getId()).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> ticket : response.body()) {
                        String ticketId = findVal(ticket, "VeID", "id");
                        if (!ticketId.isEmpty()) {
                            Map<String, Object> patchData = new HashMap<>();
                            patchData.put("TrangThaiDanhGia", "Chờ đánh giá");
                            apiService.patchTicket(ticketId, patchData).enqueue(new Callback<Void>() {
                                @Override public void onResponse(Call<Void> call, Response<Void> response) {}
                                @Override public void onFailure(Call<Void> call, Throwable t) {}
                            });
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void loadData() {
        trip = (Trip) getIntent().getSerializableExtra("trip");
        if (trip != null) {
            tvRouteName.setText(trip.getRouteName());
            tvTime.setText("Giờ xuất phát: " + trip.getTime());
            tvTotalSeats.setText("Tổng số ghế: " + trip.getSeats());
            updateStatusUI(trip.getStatus());

            fetchRouteDuration();
            updateButtonState();

            btnAssign.setOnClickListener(v -> {
                if (trip.getTaiXeID() == null || trip.getTaiXeID().isEmpty() || trip.getTaiXeID().equals("null")) {
                    Intent intent = new Intent(this, DriverSelectionActivity.class);
                    startActivityForResult(intent, 500);
                } else {
                    Intent intent = new Intent(this, TripRouteActivity.class);
                    intent.putExtra("trip_data", trip);
                    startActivity(intent);
                }
            });

            fetchPassengersFromDB();
        }
    }

    private void fetchRouteDuration() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getRoutes().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> route : response.body()) {
                        String id = findVal(route, "TuyenXeID", "id");
                        if (id.equals(trip.getTuyenXeID())) {
                            String time = findVal(route, "ThoiGian", "duration");
                            tvDuration.setText("Thời gian: " + (time.isEmpty() || time.equals("null") ? "2h00" : time));
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void fetchPassengersFromDB() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getKhachHangList().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> resK) {
                Map<String, String> nameMap = new HashMap<>();
                if (resK.isSuccessful() && resK.body() != null) {
                    for (Map<String, Object> kh : resK.body()) {
                        String id = findVal(kh, "KhachHangID", "id");
                        String name = findVal(kh, "Hovaten", "TenKhachHang");
                        if (!id.isEmpty()) nameMap.put(id, name);
                    }
                }

                apiService.getTicketsByTrip(trip.getId()).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> resV) {
                        if (resV.isSuccessful() && resV.body() != null) {
                            passengerList.clear();
                            for (Map<String, Object> map : resV.body()) {
                                String tripId = findVal(map, "ChuyenXe", "chuyenxe");
                                if (tripId.equals(trip.getId())) {
                                    String khId = findVal(map, "KhachHang", "khachhang");
                                    String name = nameMap.getOrDefault(khId, khId);
                                    String phone = findVal(map, "SoDienThoai", "phone");
                                    String pickup = findVal(map, "DiemDon", "pickup");
                                    String dropoff = findVal(map, "DiemTra", "dropoff");
                                    String fullSeatId = findVal(map, "Ghe", "MaGhe");
                                    
                                    String displaySeat = fullSeatId;
                                    if (fullSeatId.startsWith(trip.getId())) {
                                        displaySeat = fullSeatId.substring(trip.getId().length());
                                    }

                                    passengerList.add(new Passenger(name.isEmpty() ? "Hành khách" : name, phone, pickup, dropoff, displaySeat));
                                }
                            }
                            adapter.notifyDataSetChanged();
                            tvAvailableSeats.setText("Ghế trống: " + (trip.getSeats() - passengerList.size()));
                        }
                    }
                    @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
                });
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private String findVal(Map<String, Object> map, String... keys) {
        if (map == null) return "";
        for (String k : keys) {
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (e.getKey().equalsIgnoreCase(k) && e.getValue() != null) return e.getValue().toString();
            }
        }
        return "";
    }

    private void updateButtonState() {
        if (trip.getTaiXeID() != null && !trip.getTaiXeID().isEmpty() && !trip.getTaiXeID().equals("null")) {
            btnAssign.setText("Hiển thị lộ trình chuyến");
            btnAssign.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            btnAssign.setText("Phân công tài xế");
            btnAssign.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#00B0FF")));
        }
    }

    private void setupBottomNavigation() {
        View h = findViewById(R.id.nav_home_op_main);
        if (h != null) h.setOnClickListener(v -> { startActivity(new Intent(this, OperatorMainActivity.class)); finish(); });
        View t = findViewById(R.id.nav_trip_op);
        if (t != null) t.setOnClickListener(v -> { startActivity(new Intent(this, TripListActivity.class)); finish(); });
        View r = findViewById(R.id.nav_route_op);
        if (r != null) r.setOnClickListener(v -> { startActivity(new Intent(this, QLTuyenxeActivity.class)); finish(); });
        View v = findViewById(R.id.nav_vehicle_op);
        if (v != null) v.setOnClickListener(v1 -> { startActivity(new Intent(this, PhuongTienManagementActivity.class)); finish(); });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && resultCode == RESULT_OK && data != null) {
            Driver driver = (Driver) data.getSerializableExtra("selectedDriver");
            if (driver != null) updateTripDriverOnServer(driver);
        }
    }

    private void updateTripDriverOnServer(Driver driver) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, Object> data = new HashMap<>();
        data.put("ChuyenXeID", trip.getId());
        data.put("Taixe", driver.getId());
        apiService.updateTripRaw(trip.getId(), data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    trip.setTaiXeID(driver.getId());
                    showSuccessDialog();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        TextView tvMsg = dialog.findViewById(R.id.tvSuccessMessage);
        if (tvMsg != null) tvMsg.setText("Phân công tài xế thành công");
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new android.os.Handler().postDelayed(() -> {
            dialog.dismiss();
            updateButtonState();
        }, 1500);
    }
}
