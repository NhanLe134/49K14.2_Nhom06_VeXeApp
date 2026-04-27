package com.example.nhom7vexeapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.adapters.AssignDriverAdapter;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Driver;
import com.example.nhom7vexeapp.models.UserModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignDriverActivity extends AppCompatActivity implements AssignDriverAdapter.OnDriverSelectedListener {

    private RecyclerView rvDrivers;
    private AssignDriverAdapter adapter;
    private List<Driver> driverList = new ArrayList<>();
    private String tripId, currentOpId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_driver);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentOpId = pref.getString("op_uid", "");
        
        tripId = getIntent().getStringExtra("tripId");
        Log.d("AssignDriver", "Received Trip ID: " + tripId);

        if (tripId == null || tripId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID chuyến xe!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        apiService = ApiClient.getClient().create(ApiService.class);

        rvDrivers = findViewById(R.id.rvAssignDrivers);
        rvDrivers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignDriverAdapter(driverList, this);
        rvDrivers.setAdapter(adapter);

        loadDriversForAssign();
        setupBottomNavigation();
    }

    private void loadDriversForAssign() {
        apiService.getUsers("Get").enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> resU) {
                Map<String, String> phoneMap = new HashMap<>();
                if (resU.isSuccessful() && resU.body() != null) {
                    for (UserModel u : resU.body()) {
                        if (u.getTaixe() != null) phoneMap.put(u.getTaixe(), u.getSoDienThoai());
                    }
                }

                apiService.getChiTietTaiXe().enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> resC) {
                        if (resC.isSuccessful() && resC.body() != null) {
                            driverList.clear();
                            for (Map<String, Object> map : resC.body()) {
                                String nxeId = findVal(map, "Nhaxe", "nhaxe", "NhaXeID");
                                if (nxeId.equalsIgnoreCase(currentOpId)) {
                                    String txId = findVal(map, "Taixe", "TaiXeID", "taixe", "id");
                                    String name = findVal(map, "HoTen", "hoten", "TenTaiXe");
                                    String phone = phoneMap.getOrDefault(txId, "090XXXXXXXX");
                                    if (!txId.isEmpty()) {
                                        driverList.add(new Driver(txId, name, phone, ""));
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                            if (driverList.isEmpty()) {
                                Log.w("AssignDriver", "No drivers found for Operator: " + currentOpId);
                            }
                        }
                    }
                    @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        Log.e("AssignDriver", "Failed to fetch drivers", t);
                    }
                });
            }
            @Override public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Log.e("AssignDriver", "Failed to fetch users", t);
            }
        });
    }

    private String findVal(Map<String, Object> map, String... keys) {
        if (map == null) return "";
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) {
                if (v instanceof Map) {
                    return findVal((Map<String, Object>) v, "id", "ID", "TaiXeID");
                }
                return v.toString();
            }
        }
        return "";
    }

    @Override
    public void onDriverSelected(Driver driver) {
        showConfirmDialog(driver);
    }

    private void showConfirmDialog(Driver driver) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_assign);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvMsg = dialog.findViewById(R.id.tvConfirmMessage);
        if (tvMsg != null) tvMsg.setText("Bạn có muốn phân công tài xế " + driver.getName() + "?");

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            assignDriverToTrip(driver);
        });
        dialog.show();
    }

    private void assignDriverToTrip(Driver driver) {
        Map<String, Object> data = new HashMap<>();
        data.put("Taixe", driver.getId());

        apiService.patchTrip(tripId, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else {
                    Toast.makeText(AssignDriverActivity.this, "Lỗi phân công!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AssignDriverActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMsg = dialog.findViewById(R.id.tvMessage);
        if (tvMsg != null) {
            tvMsg.setText("Phân công tài xế thành công");
        }

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        
        dialog.setOnDismissListener(d -> {
            setResult(RESULT_OK);
            finish();
        });

        dialog.show();

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 2000);
    }

    private void setupBottomNavigation() {
        View navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorMainActivity.class));
                finish();
            });
        }
        
        View navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                startActivity(new Intent(this, TripListActivity.class));
                finish();
            });
        }
    }
}
