package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.adapters.VehicleManagedAdapter;
import com.example.nhom7vexeapp.models.VehicleManaged;
import com.example.nhom7vexeapp.viewmodels.VehicleViewModel;

import java.util.ArrayList;
import java.util.List;

public class QLPhuongTienActivity extends AppCompatActivity {

    private RecyclerView rvVehicles;
    private VehicleManagedAdapter adapter;
    private List<VehicleManaged> vehicleList = new ArrayList<>();
    private ProgressBar progressBar;
    private VehicleViewModel viewModel;
    private String nhaXeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_phuong_tien);

        viewModel = new ViewModelProvider(this).get(VehicleViewModel.class);

        initViews();
        setupRecyclerView();
        setupObservers();
        setupBottomNavigation();

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        nhaXeId = pref.getString("op_uid", "NX00001"); 

        viewModel.fetchVehicles(nhaXeId);
    }

    private void initViews() {
        rvVehicles = findViewById(R.id.rvVehicles);
        progressBar = findViewById(R.id.progressBarVehicles);
        
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        LinearLayout btnAddVehicle = findViewById(R.id.btnAddVehicle);
        if (btnAddVehicle != null) {
            btnAddVehicle.setOnClickListener(v -> {
                Intent intent = new Intent(QLPhuongTienActivity.this, CreateVehicleActivity.class);
                startActivityForResult(intent, 1001);
            });
        }

        View imgOpProfile = findViewById(R.id.imgOpProfile);
        if (imgOpProfile != null) {
            imgOpProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorProfileActivity.class));
            });
        }
    }

    private void setupRecyclerView() {
        if (rvVehicles != null) {
            rvVehicles.setLayoutManager(new LinearLayoutManager(this));
            adapter = new VehicleManagedAdapter(vehicleList, this, new VehicleManagedAdapter.OnVehicleActionListener() {
                @Override
                public void onDelete(VehicleManaged vehicle, int position) {
                    viewModel.deleteVehicle(vehicle.getXeID());
                }

                @Override
                public void onShowDetail(VehicleManaged vehicle) {
                    Intent intent = new Intent(QLPhuongTienActivity.this, VehicleDetailActivity.class);
                    intent.putExtra("vehicle_managed_data", vehicle);
                    startActivity(intent);
                }
            });
            rvVehicles.setAdapter(adapter);
        }
    }

    private void setupObservers() {
        viewModel.vehicleList.observe(this, newList -> {
            if (newList != null) {
                vehicleList.clear();
                vehicleList.addAll(newList);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        viewModel.isActionSuccess.observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Thao tác thành công!", Toast.LENGTH_SHORT).show();
                viewModel.fetchVehicles(nhaXeId);
            }
        });
    }

    private void setupBottomNavigation() {
        // TRANG CHỦ
        View navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, OperatorMainActivity.class));
            finish();
        });

        // TÀI XẾ
        View navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) navDriver.setOnClickListener(v -> {
            startActivity(new Intent(this, DriverSelectionActivity.class));
            finish();
        });

        // PHƯƠNG TIỆN (Chuyển đến màn hình quản lý chung)
        View navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) navVehicle.setOnClickListener(v -> {
            startActivity(new Intent(this, PhuongTienManagementActivity.class));
            finish();
        });

        // CHUYẾN XE
        View navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) navTrip.setOnClickListener(v -> {
            startActivity(new Intent(this, TripListActivity.class));
            finish();
        });

        // TUYẾN XE
        View navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) navRoute.setOnClickListener(v -> {
            startActivity(new Intent(this, QLTuyenxeActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            viewModel.fetchVehicles(nhaXeId);
        }
    }
}
