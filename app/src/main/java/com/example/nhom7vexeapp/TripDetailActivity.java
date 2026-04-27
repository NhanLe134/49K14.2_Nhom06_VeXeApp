package com.example.nhom7vexeapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
    private int position; // Từ file 1
    private List<Passenger> passengerList = new ArrayList<>();
    private PassengerAdapter adapter;
    private ImageView imgOpProfile, btnBack;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        apiService = ApiClient.getClient().create(ApiService.class);

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

        // Khởi tạo RecyclerView (File 2)
        if (rvPassengers != null) {
            rvPassengers.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PassengerAdapter(passengerList);
            rvPassengers.setAdapter(adapter);
        }

        // Sự kiện Profile (File 2)
        if (imgOpProfile != null) {
            imgOpProfile.setOnClickListener(v -> startActivity(new Intent(this, OperatorProfileActivity.class)));
        }

        // Sự kiện Back (File 2)
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Logic đổi trạng thái chuyến xe (File 2)
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
            tvStatusOption.setOnClickListener(v -> showConfirmStatusDialog(tvStatusOption.getText().toString()));
        }
    }

    private void loadInitialData() {
        trip = (Trip) getIntent().getSerializableExtra("trip");
        position = getIntent().getIntExtra("position", -1);

        if (trip != null) {
            updateUI();
            refreshTripData(); // Lấy data mới nhất từ server
        }
    }

    private void updateUI() {
        if (trip == null) return;
        if (tvRouteName != null) tvRouteName.setText("Tuyến xe: " + trip.getRouteName());
        if (tvTime != null) tvTime.setText("Giờ xuất phát: " + trip.getTime());
        if (tvTotalSeats != null) tvTotalSeats.setText("Tổng số ghế: " + trip.getSeats());

        updateStatusUI(trip.getStatus());
        updateButtonState();
        fetchPassengersFromDB(); // Lấy danh sách hành khách thực tế
    }

    private void updateButtonState() {
        if (btnAssign == null || trip == null) return;

        // Kiểm tra xem đã có tài xế chưa (kết hợp cả TaiXeID và getAssignedDriver)
        boolean hasDriver = (trip.getAssignedDriver() != null) ||
                (trip.getTaiXeID() != null && !trip.getTaiXeID().isEmpty() && !trip.getTaiXeID().equals("null"));

        if (hasDriver) {
            btnAssign.setText("Hiển thị lộ trình chuyến");
            btnAssign.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btnAssign.setOnClickListener(v -> {
                Intent intent = new Intent(this, TripRouteActivity.class);
                intent.putExtra("trip_data", trip);
                startActivity(intent);
            });
        } else {
            btnAssign.setText("Phân công tài xế");
            btnAssign.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#00B0FF")));
            btnAssign.setOnClickListener(v -> {
                Intent intent = new Intent(this, DriverSelectionActivity.class);
                intent.putExtra("tripId", trip.getId());
                startActivityForResult(intent, 500);
            });
        }
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

    private void fetchPassengersFromDB() {
        if (trip == null) return;
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

    private void refreshTripData() {
        if (trip == null) return;
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

    private void updateTripStatusOnServer(String newStatus) {
        if (trip == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("TrangThai", newStatus);

        apiService.patchTrip(trip.getId(), data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    trip.setStatus(newStatus);
                    updateStatusUI(newStatus);
                    setResult(RESULT_OK);
                    if (newStatus.equalsIgnoreCase("Hoàn thành")) updateAllTicketsToPendingReview();
                    Toast.makeText(TripDetailActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TripDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAllTicketsToPendingReview() {
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

    private void showConfirmStatusDialog(String newStatus) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_status);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View btnCancel = dialog.findViewById(R.id.btnCancel);
        View btnConfirm = dialog.findViewById(R.id.btnConfirm);

        if (btnCancel != null) btnCancel.setOnClickListener(v -> {
            if (tvStatusOption != null) tvStatusOption.setVisibility(View.GONE);
            dialog.dismiss();
        });

        if (btnConfirm != null) btnConfirm.setOnClickListener(v -> {
            updateTripStatusOnServer(newStatus);
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selectedDriver")) {
                Driver driver = (Driver) data.getSerializableExtra("selectedDriver");
                showSuccessDialog(driver);
            } else {
                refreshTripData();
            }
            setResult(RESULT_OK);
        }
    }

    private void showSuccessDialog(Driver driver) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        TextView tvMsg = dialog.findViewById(R.id.tvMessage);
        if (tvMsg != null) tvMsg.setText("Phân công tài xế thành công");
        dialog.show();

        new android.os.Handler().postDelayed(() -> {
            dialog.dismiss();
            trip.setAssignedDriver(driver);
            updateButtonState();

            Intent intent = new Intent();
            intent.putExtra("updatedTrip", trip);
            intent.putExtra("position", position);
            setResult(RESULT_OK, intent);
        }, 1500);
    }

    private void setupBottomNavigation() {
        // Hợp nhất các sự kiện click từ File 2
        int[] ids = {R.id.nav_home_op_main, R.id.nav_trip_op, R.id.nav_driver_op, R.id.nav_vehicle_op, R.id.nav_route_op};
        Class<?>[] classes = {OperatorMainActivity.class, TripListActivity.class, QLNhaxeActivity.class, PhuongTienManagementActivity.class, QLTuyenxeActivity.class};

        for (int i = 0; i < ids.length; i++) {
            View nav = findViewById(ids[i]);
            if (nav != null) {
                final int index = i;
                nav.setOnClickListener(v -> {
                    Intent intent = new Intent(this, classes[index]);
                    if (index == 0) intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        }
    }

    private String findVal(Map<String, Object> map, String... keys) {
        if (map == null) return "";
        for (String k : keys) {
            if (map.containsKey(k) && map.get(k) != null) return map.get(k).toString();
        }
        return "";
    }
}