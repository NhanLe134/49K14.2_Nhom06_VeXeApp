package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.adapters.CarTypeAdapter;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Loaixe;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarTypeManagementActivity extends AppCompatActivity {

    private RecyclerView rvCarTypes;
    private CarTypeAdapter adapter;
    private List<Loaixe> carTypeList;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_type_management);

        initViews();
        setupRecyclerView();
        fetchCarTypes(); // Gọi hàm lấy dữ liệu từ API
        setupEvents();
        setupBottomNav();
    }

    private void initViews() {
        rvCarTypes = findViewById(R.id.rvCarTypes);
        btnBack = findViewById(R.id.btnBack);
        if (btnBack == null) btnBack = findViewById(R.id.btnProfile);
    }

    private void setupEvents() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        carTypeList = new ArrayList<>();
        adapter = new CarTypeAdapter(carTypeList, this);
        rvCarTypes.setLayoutManager(new LinearLayoutManager(this));
        rvCarTypes.setAdapter(adapter);
    }

    private void fetchCarTypes() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getLoaixe().enqueue(new Callback<List<Loaixe>>() {
            @Override
            public void onResponse(Call<List<Loaixe>> call, Response<List<Loaixe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    carTypeList.clear();
                    carTypeList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CarTypeManagementActivity.this, "Không thể lấy dữ liệu từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Loaixe>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(CarTypeManagementActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home_op);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        LinearLayout navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                Intent intent = new Intent(this, PhuongTienManagementActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> startActivity(new Intent(this, TripListActivity.class)));
        }

        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> startActivity(new Intent(this, QLTuyenxeActivity.class)));
        }

        LinearLayout navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> startActivity(new Intent(this, DriverSelectionActivity.class)));
        }
    }
}
