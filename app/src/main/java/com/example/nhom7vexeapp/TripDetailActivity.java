package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import com.example.nhom7vexeapp.models.Passenger;
import com.example.nhom7vexeapp.models.Seat;
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

    private TextView tvRouteName, tvTime, tvTotalSeats, tvAvailableSeats, tvStatus;
    private MaterialButton btnAssign;
    private RecyclerView rvPassengers;
    private Trip trip;
    private List<Passenger> passengerList = new ArrayList<>();
    private PassengerAdapter adapter;
    private ImageView imgLogo;

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
        tvTotalSeats = findViewById(R.id.tvTotalSeats);
        tvAvailableSeats = findViewById(R.id.tvAvailableSeats);
        tvStatus = findViewById(R.id.tvStatus);
        btnAssign = findViewById(R.id.btnAssign);
        rvPassengers = findViewById(R.id.rvPassengers);
        imgLogo = findViewById(R.id.imgLogo);

        rvPassengers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PassengerAdapter(passengerList);
        rvPassengers.setAdapter(adapter);

        if (imgLogo != null) imgLogo.setOnClickListener(v -> finish());
    }

    private void loadData() {
        trip = (Trip) getIntent().getSerializableExtra("trip");
        if (trip != null) {
            tvRouteName.setText(trip.getRouteName());
            tvTime.setText("Giờ xuất phát: " + trip.getTime());
            tvTotalSeats.setText("Tổng số ghế: " + trip.getSeats());
            tvStatus.setText(trip.getStatus());
            updateButtonState();

            btnAssign.setOnClickListener(v -> {
                if (trip.getTaiXeID() == null || trip.getTaiXeID().isEmpty() || trip.getTaiXeID().equals("null")) {
                    Intent intent = new Intent(this, AssignDriverActivity.class);
                    intent.putExtra("tripId", trip.getId());
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

    private void fetchPassengersFromDB() {
        if (trip == null) return;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // 1. LẤY TÊN KHÁCH HÀNG
        apiService.getKhachHangList().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> resK) {
                final Map<String, String> khNames = new HashMap<>();
                if (resK.isSuccessful() && resK.body() != null) {
                    for (Map<String, Object> kh : resK.body()) {
                        String id = findVal(kh, "KhachHangID", "id");
                        String name = findVal(kh, "Hovaten", "TenKhachHang", "hoTen");
                        if (!id.isEmpty()) khNames.put(id, name);
                    }
                }

                // 2. LẤY DANH SÁCH GHẾ (DÙNG MODEL SEAT)
                apiService.getSeatsByTrip(trip.getId()).enqueue(new Callback<List<Seat>>() {
                    @Override
                    public void onResponse(Call<List<Seat>> call, Response<List<Seat>> resG) {
                        final List<Seat> allSeatsRaw = (resG.body() != null) ? resG.body() : new ArrayList<>();

                        // 3. LẤY DANH SÁCH VÉ
                        apiService.getTicketsByTrip(trip.getId()).enqueue(new Callback<List<Map<String, Object>>>() {
                            @Override
                            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    passengerList.clear();
                                    for (Map<String, Object> v : response.body()) {
                                        // KIỂM TRA ĐÚNG CHUYẾN XE
                                        String ticketTripId = findVal(v, "ChuyenXe", "chuyenxe", "ChuyenXeID");
                                        if (!ticketTripId.equalsIgnoreCase(trip.getId())) continue;

                                        String veId = findVal(v, "VeID", "id", "ve_id", "VEID");
                                        String khId = findVal(v, "KhachHang", "khachhang", "KhachHangID");
                                        String name = khNames.getOrDefault(khId, "Hành khách");
                                        String phone = findVal(v, "SoDienThoai", "phone", "sdt");
                                        String pickup = findVal(v, "DiemDon", "pickup", "diem_don");
                                        String dropoff = findVal(v, "DiemTra", "dropoff", "diem_tra");

                                        // ✅ KHỚP SỐ GHẾ (soGhe) TỪ BẢNG GHẾ DỰA TRÊN VeID
                                        List<String> seatCodes = new ArrayList<>();
                                        for (Seat s : allSeatsRaw) {
                                            String sVeId = s.getTicketId();
                                            if (!sVeId.isEmpty() && sVeId.equalsIgnoreCase(veId)) {
                                                String seatCode = s.getSeatCode();
                                                if (!seatCode.isEmpty()) seatCodes.add(seatCode);
                                            }
                                        }

                                        // Fallback 1: Thử lấy trực tiếp từ bảng Vé (Cột "DANH SÁCH GHẾ")
                                        if (seatCodes.isEmpty()) {
                                            String direct = findVal(v, "DanhSachGhe", "danh_sach_ghe", "SOGHE", "soGhe");
                                            if (!direct.isEmpty()) seatCodes.add(direct);
                                        }

                                        // Fallback 2: Thử lấy ID ghế định danh trong vé rồi tra cứu
                                        if (seatCodes.isEmpty()) {
                                            String gId = findVal(v, "Ghe", "ghe", "MaGhe");
                                            for (Seat s : allSeatsRaw) {
                                                String sId = s.getId();
                                                if (!sId.isEmpty() && sId.equalsIgnoreCase(gId)) {
                                                    String code = s.getSeatCode();
                                                    if (!code.isEmpty()) seatCodes.add(code);
                                                    break;
                                                }
                                            }
                                        }

                                        String displaySeat = (seatCodes.isEmpty()) ? "??" : android.text.TextUtils.join(", ", seatCodes);
                                        passengerList.add(new Passenger(name, phone, pickup, dropoff, displaySeat));
                                    }
                                    adapter.notifyDataSetChanged();
                                    if (tvAvailableSeats != null) 
                                        tvAvailableSeats.setText("Ghế trống: " + (trip.getSeats() - passengerList.size()));
                                }
                            }
                            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
                        });
                    }
                    @Override public void onFailure(Call<List<Seat>> call, Throwable t) {}
                });
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
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

    private String findVal(Object obj, String... keys) {
        if (obj == null) return "";
        if (obj instanceof String) return (String) obj;
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            for (String k : keys) {
                Object val = map.get(k);
                if (val == null) {
                    for (String key : map.keySet()) {
                        if (key.equalsIgnoreCase(k)) {
                            val = map.get(key);
                            break;
                        }
                    }
                }
                if (val != null) {
                    if (val instanceof Map || val instanceof List) {
                        String deep = findVal(val, "id", "VeID", "ve_id", "VEID", "gheID", "soGhe", "SOGHE", "KhachHangID");
                        if (!deep.isEmpty()) return deep;
                    }
                    return val.toString();
                }
            }
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            if (!list.isEmpty()) return findVal(list.get(0), keys);
        }
        return "";
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home_op_main).setOnClickListener(v -> finish());
        findViewById(R.id.nav_trip_op).setOnClickListener(v -> startActivity(new Intent(this, TripListActivity.class)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && resultCode == RESULT_OK) loadData();
    }
}
