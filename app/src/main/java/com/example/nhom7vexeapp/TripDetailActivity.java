package com.example.nhom7vexeapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

    private TextView tvRouteName, tvTime, tvTotalSeats, tvAvailableSeats, tvStatus, tvStatusOption;
    private MaterialButton btnAssign;
    private RecyclerView rvPassengers;
    private Trip trip;
    private int position;
    private List<Passenger> passengerList = new ArrayList<>();
    private PassengerAdapter adapter;
    private ImageView btnBack;

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
        tvTotalSeats = findViewById(R.id.tvTotalSeats);
        tvAvailableSeats = findViewById(R.id.tvAvailableSeats);
        tvStatus = findViewById(R.id.tvStatus);
        tvStatusOption = findViewById(R.id.tvStatusOption);
        btnAssign = findViewById(R.id.btnAssign);
        rvPassengers = findViewById(R.id.rvPassengers);
        btnBack = findViewById(R.id.btnBack);

        if (rvPassengers != null) {
            rvPassengers.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PassengerAdapter(passengerList);
            rvPassengers.setAdapter(adapter);
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Logic thay đổi trạng thái Chuyến xe (Hoàn thành / Chưa hoàn thành)
        if (tvStatus != null) {
            tvStatus.setOnClickListener(v -> {
                if (trip == null || tvStatusOption == null) return;
                if (tvStatusOption.getVisibility() == View.VISIBLE) {
                    tvStatusOption.setVisibility(View.GONE);
                } else {
                    String currentStatus = trip.getStatus() != null ? trip.getStatus() : "";
                    String optionText = currentStatus.equalsIgnoreCase("Hoàn thành") ? "Chưa hoàn thành" : "Hoàn thành";
                    tvStatusOption.setText(optionText);
                    tvStatusOption.setVisibility(View.VISIBLE);
                }
            });
        }

        if (tvStatusOption != null) {
            tvStatusOption.setOnClickListener(v -> {
                updateTripStatusOnServer(tvStatusOption.getText().toString());
            });
        }
    }

    private void loadInitialData() {
        trip = (Trip) getIntent().getSerializableExtra("trip");
        position = getIntent().getIntExtra("position", -1);

        if (trip != null) {
            updateUI();
            fetchPassengersFromDB();
        }
    }

    private void updateUI() {
        if (trip == null) return;
        tvRouteName.setText(trip.getRouteName());
        tvTime.setText("Giờ xuất phát: " + trip.getTime());
        tvTotalSeats.setText("Tổng số ghế: " + trip.getSeats());
        tvStatus.setText(trip.getStatus());

        // Cập nhật màu sắc trạng thái
        if (trip.getStatus().equalsIgnoreCase("Hoàn thành")) {
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvStatus.setTextColor(Color.parseColor("#FBC02D"));
        }

        // Cập nhật nút Phân công tài xế
        if (trip.getTaiXeID() != null && !trip.getTaiXeID().isEmpty() && !trip.getTaiXeID().equals("null")) {
            btnAssign.setText("Hiển thị lộ trình chuyến");
        } else {
            btnAssign.setText("Phân công tài xế");
        }

        btnAssign.setOnClickListener(v -> {
            if (trip.getTaiXeID() == null || trip.getTaiXeID().isEmpty() || trip.getTaiXeID().equals("null")) {
                Intent intent = new Intent(this, DriverSelectionActivity.class);
                intent.putExtra("tripId", trip.getId());
                startActivityForResult(intent, 500);
            } else {
                Toast.makeText(this, "Tính năng lộ trình đang được phát triển", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPassengersFromDB() {
        if (trip == null) return;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Gọi API lấy danh sách vé của chuyến xe này
        apiService.getChuyenXe().enqueue(new Callback<List<com.example.nhom7vexeapp.models.TripSearchResult>>() {
            @Override
            public void onResponse(Call<List<com.example.nhom7vexeapp.models.TripSearchResult>> call, Response<List<com.example.nhom7vexeapp.models.TripSearchResult>> response) {
                // Ở đây chúng ta sẽ lấy danh sách vé từ server dựa trên trip.getId()
                // Do ApiService hiện tại chưa có hàm getTicketsByTrip, tôi sẽ để logic này chờ cập nhật
                // Tạm thời hiển thị danh sách trống hoặc mock nếu cần.
            }

            @Override
            public void onFailure(Call<List<com.example.nhom7vexeapp.models.TripSearchResult>> call, Throwable t) {}
        });
    }

    private void updateTripStatusOnServer(String newStatus) {
        // Logic gọi PATCH API để cập nhật trạng thái chuyến xe
        // Sau khi thành công:
        trip.setStatus(newStatus);
        updateUI();
        if (tvStatusOption != null) tvStatusOption.setVisibility(View.GONE);
        Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && resultCode == RESULT_OK) {
            // Cập nhật lại dữ liệu sau khi phân công tài xế
            setResult(RESULT_OK);
            finish();
        }
    }

    private void setupBottomNavigation() {
        View navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
    }
}
