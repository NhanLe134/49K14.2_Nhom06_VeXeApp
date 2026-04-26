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
import android.widget.LinearLayout;
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
    private ImageView imgOpProfile, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        initViews();
        loadInitialData();
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
        btnBack = findViewById(R.id.btnBack);
        
        if (rvPassengers != null) {
            rvPassengers.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PassengerAdapter(passengerList);
            rvPassengers.setAdapter(adapter);
        }

        if (imgOpProfile != null) {
            imgOpProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorProfileActivity.class));
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tvStatus != null) {
            tvStatus.setOnClickListener(v -> {
                if (trip == null || tvStatusOption == null) return;
                if (tvStatusOption.getVisibility() == View.VISIBLE) {
                    tvStatusOption.setVisibility(View.GONE);
                } else {
                    String currentStatus = trip.getStatus() != null ? trip.getStatus() : "";
                    String optionText = currentStatus.equalsIgnoreCase("Hoàn thành") ? "Chưa hoàn thành" : "Hoàn thành";
                    tvStatusOption.setText(optionText);
                    tvStatusOption.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            optionText.equalsIgnoreCase("Hoàn thành") ? Color.parseColor("#E8F5E9") : Color.parseColor("#FFF9C4")));
                    tvStatusOption.setTextColor(optionText.equalsIgnoreCase("Hoàn thành") ? Color.parseColor("#4CAF50") : Color.parseColor("#FBC02D"));
                    tvStatusOption.setVisibility(View.VISIBLE);
                }
            });
        }

        if (tvStatusOption != null) {
            tvStatusOption.setOnClickListener(v -> {
                showConfirmStatusDialog(tvStatusOption.getText().toString());
            });
        }
    }

    private void showConfirmStatusDialog(String newStatus) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_status);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View btnCancel = dialog.findViewById(R.id.btnCancel);
        View btnConfirm = dialog.findViewById(R.id.btnConfirm);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (tvStatusOption != null) tvStatusOption.setVisibility(View.GONE);
                dialog.dismiss();
            });
        }
        
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                updateTripStatusOnServer(newStatus);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void updateTripStatusOnServer(String newStatus) {
        if (trip == null) return;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, Object> data = new HashMap<>();
        data.put("TrangThai", newStatus);

        apiService.patchTrip(trip.getId(), data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    trip.setStatus(newStatus);
                    updateStatusUI(newStatus);
                    setResult(RESULT_OK);
                    
                    // ✅ CẬP NHẬT TRẠNG THÁI ĐÁNH GIÁ CỦA VÉ KHI CHUYẾN XE HOÀN THÀNH
                    if (newStatus.equalsIgnoreCase("Hoàn thành")) {
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

    private void updateAllTicketsToPendingReview() {
        if (trip == null) return;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getTicketsByTrip(trip.getId()).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> ticket : response.body()) {
                        String ticketId = findVal(ticket, "VeID", "id", "veid");
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

    private void updateStatusUI(String status) {
        if (tvStatus == null) return;
        tvStatus.setText(status);
        if (status != null && status.equalsIgnoreCase("Hoàn thành")) {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF9C4")));
            tvStatus.setTextColor(Color.parseColor("#FBC02D"));
        }
        if (tvStatusOption != null) tvStatusOption.setVisibility(View.GONE);
    }

    private void loadInitialData() {
        trip = (Trip) getIntent().getSerializableExtra("trip");
        if (trip != null) {
            updateUI();
            refreshTripData();
        }
    }

    private void refreshTripData() {
        if (trip == null) return;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getTrips().enqueue(new Callback<List<Trip>>() {
            @Override
            public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Trip t : response.body()) {
                        if (t.getId().equals(trip.getId())) {
                            trip = t;
                            updateUI();
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<Trip>> call, Throwable t) {}
        });
    }

    private void updateUI() {
        if (trip == null) return;
        if (tvRouteName != null) tvRouteName.setText(trip.getRouteName());
        if (tvTime != null) tvTime.setText("Giờ xuất phát: " + trip.getTime());
        if (tvTotalSeats != null) tvTotalSeats.setText("Tổng số ghế: " + trip.getSeats());
        updateStatusUI(trip.getStatus());
        updateButtonState();
        fetchPassengersFromDB();
        
        if (btnAssign != null) {
            btnAssign.setOnClickListener(v -> {
                if (trip.getTaiXeID() == null || trip.getTaiXeID().isEmpty() || trip.getTaiXeID().equals("null")) {
                    Intent intent = new Intent(this, DriverSelectionActivity.class);
                    intent.putExtra("tripId", trip.getId());
                    startActivityForResult(intent, 500);
                } else {
                    Intent intent = new Intent(this, TripRouteActivity.class);
                    intent.putExtra("trip_data", trip);
                    startActivity(intent);
                }
            });
        }
    }

    private void updateButtonState() {
        if (btnAssign == null || trip == null) return;
        if (trip.getTaiXeID() != null && !trip.getTaiXeID().isEmpty() && !trip.getTaiXeID().equals("null")) {
            btnAssign.setText("Hiển thị lộ trình chuyến");
            btnAssign.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            btnAssign.setText("Phân công tài xế");
            btnAssign.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#00B0FF")));
        }
    }

    private void fetchPassengersFromDB() {
        if (trip == null) return;
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
                                String tId = findVal(map, "ChuyenXe", "chuyenxe");
                                if (tId.equals(trip.getId())) {
                                    String khId = findVal(map, "KhachHang", "khachhang");
                                    String name = nameMap.getOrDefault(khId, "Khách hàng " + khId);
                                    String phone = findVal(map, "SoDienThoai", "phone");
                                    String pickup = findVal(map, "DiemDon", "pickup");
                                    String dropoff = findVal(map, "DiemTra", "dropoff");
                                    String seat = findVal(map, "Ghe", "MaGhe");
                                    
                                    String displaySeat = seat;
                                    if (seat.startsWith(trip.getId())) {
                                        displaySeat = seat.substring(trip.getId().length());
                                    }
                                    
                                    passengerList.add(new Passenger(name, phone, pickup, dropoff, displaySeat));
                                }
                            }
                            if (adapter != null) adapter.notifyDataSetChanged();
                            if (tvAvailableSeats != null) tvAvailableSeats.setText("Ghế trống: " + (trip.getSeats() - passengerList.size()));
                        }
                    }
                    @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
                });
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, OperatorMainActivity.class));
            finish();
        });

        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) navTrip.setOnClickListener(v -> {
            startActivity(new Intent(this, TripListActivity.class));
            finish();
        });

        LinearLayout navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) navDriver.setOnClickListener(v -> {
            startActivity(new Intent(this, QLNhaxeActivity.class));
            finish();
        });

        LinearLayout navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) navVehicle.setOnClickListener(v -> {
            startActivity(new Intent(this, PhuongTienManagementActivity.class));
            finish();
        });

        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) navRoute.setOnClickListener(v -> {
            startActivity(new Intent(this, QLTuyenxeActivity.class));
            finish();
        });
    }

    private String findVal(Map<String, Object> map, String... keys) {
        if (map == null) return "";
        for (String k : keys) {
            if (map.containsKey(k) && map.get(k) != null) return map.get(k).toString();
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            refreshTripData();
        }
    }
}
