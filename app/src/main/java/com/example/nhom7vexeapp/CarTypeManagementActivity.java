package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private ImageView btnBack, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_type_management);

        initViews();
        setupRecyclerView();
        fetchCarTypes(); 
        setupEvents();
        setupBottomNav();
    }

    private void initViews() {
        rvCarTypes = findViewById(R.id.rvCarTypes);
        btnBack = findViewById(R.id.btnBack);
        btnProfile = findViewById(R.id.btnProfile);
        // Fallback if ID is different in XML
        if (btnProfile == null) btnProfile = findViewById(R.id.imgOpProfile);
    }

    private void setupEvents() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorProfileActivity.class));
            });
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
        // TRANG CHỦ
        View navHome = findViewById(R.id.nav_home_op_main);
        if (navHome == null) navHome = findViewById(R.id.nav_home_op);
        
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorMainActivity.class));
                finish();
            });
        }

        // TÀI XẾ (Đã sửa từ QLNhaxeActivity sang DriverSelectionActivity)
        LinearLayout navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> {
                startActivity(new Intent(this, DriverSelectionActivity.class));
                finish();
            });
        }

        // PHƯƠNG TIỆN
        LinearLayout navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                startActivity(new Intent(this, PhuongTienManagementActivity.class));
                finish();
            });
        }

        // CHUYẾN XE
        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                startActivity(new Intent(this, TripListActivity.class));
                finish();
            });
        }

        // TUYẾN XE
        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                startActivity(new Intent(this, QLTuyenxeActivity.class));
                finish();
            });
        }
    }
}
