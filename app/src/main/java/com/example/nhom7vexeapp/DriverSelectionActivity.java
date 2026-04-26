package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.adapters.DriverAdapter;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverSelectionActivity extends AppCompatActivity implements DriverAdapter.OnDriverClickListener {

    private RecyclerView rvDrivers;
    private DriverAdapter adapter;
    private List<Driver> driverList = new ArrayList<>();
    private String opUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_selection);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        opUid = pref.getString("op_uid", "NX00001");

        rvDrivers = findViewById(R.id.rvDrivers);
        rvDrivers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DriverAdapter(driverList, this);
        rvDrivers.setAdapter(adapter);

        loadDriversWithDetails();
        setupBottomNavigation();
    }

    private void loadDriversWithDetails() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        // 1. Tải bảng Chi Tiết Tài Xế để lấy Họ Tên
        apiService.getChiTietTaiXe().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> resCT) {
                Map<String, String> nameMap = new HashMap<>();
                if (resCT.isSuccessful() && resCT.body() != null) {
                    for (Map<String, Object> ct : resCT.body()) {
                        String id = findVal(ct, "Taixe", "TaiXeID");
                        String name = findVal(ct, "HoTen", "hoten");
                        if (!id.isEmpty()) nameMap.put(id, name);
                    }
                }

                // 2. Tải bảng Tài Xế để lấy SĐT
                apiService.getDriversRaw().enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> resTX) {
                        if (resTX.isSuccessful() && resTX.body() != null) {
                            driverList.clear();
                            for (Map<String, Object> tx : resTX.body()) {
                                String nxe = findVal(tx, "Nhaxe", "nhaxe");
                                if (nxe.isEmpty() || nxe.equalsIgnoreCase(opUid)) {
                                    String id = findVal(tx, "TaiXeID", "id");
                                    String phone = findVal(tx, "SoDienThoai", "phone");
                                    String name = nameMap.getOrDefault(id, id); // Lấy tên từ map, nếu không có thì dùng ID

                                    if (!id.isEmpty()) {
                                        driverList.add(new Driver(id, name, phone));
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
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

    @Override
    public void onDriverClick(Driver driver) {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Bạn có muốn phân công tài xế " + driver.getName() + " cho chuyến xe này?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedDriver", driver);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void setupBottomNavigation() {
        View v = findViewById(R.id.nav_home_op_main);
        if (v != null) v.setOnClickListener(v1 -> {
            startActivity(new Intent(this, OperatorMainActivity.class));
            finish();
        });
    }
}
