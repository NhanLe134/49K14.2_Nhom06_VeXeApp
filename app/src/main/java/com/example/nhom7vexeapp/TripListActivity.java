package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.adapters.TripAdapter;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Route;
import com.example.nhom7vexeapp.models.Trip;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripListActivity extends AppCompatActivity implements TripAdapter.OnTripActionListener {
    private static final String TAG = "TripListAct";
    private RecyclerView rvTrips;
    private TripAdapter adapter;
    private List<Trip> tripList = new ArrayList<>();
    private ApiService apiService;
    private String opUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        opUid = pref.getString("op_uid", "");

        apiService = ApiClient.getClient().create(ApiService.class);

        rvTrips = findViewById(R.id.rvTrips);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new TripAdapter(tripList, this);
        rvTrips.setAdapter(adapter);

        loadTripsByOperator();

        Button btnCreateTrip = findViewById(R.id.btnCreateTrip);
        if (btnCreateTrip != null) {
            btnCreateTrip.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateTripActivity.class);
                startActivityForResult(intent, 100);
            });
        }

        findViewById(R.id.imgOpProfile).setOnClickListener(v -> startActivity(new Intent(this, OperatorProfileActivity.class)));
        findViewById(R.id.imgLogo).setOnClickListener(v -> startActivity(new Intent(this, OperatorMainActivity.class)));
        
        setupBottomNavigation();
    }

    private void loadTripsByOperator() {
        if (opUid.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID nhà xe!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 1: Lấy tất cả tuyến xe để tìm xem tuyến nào thuộc nhà xe này
        apiService.getRoutes().enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> resR) {
                List<String> myRouteIds = new ArrayList<>();
                if (resR.isSuccessful() && resR.body() != null) {
                    for (Route r : resR.body()) {
                        // Kiểm tra nếu tuyến xe thuộc nhà xe hiện tại qua nhaXeId trong model Route
                        if (r.getNhaXeId() != null && r.getNhaXeId().equalsIgnoreCase(opUid)) {
                            String rid = r.getId();
                            if (rid != null && !rid.isEmpty()) {
                                myRouteIds.add(rid);
                            }
                        }
                    }
                }

                // Bước 2: Tải tất cả chuyến xe và lọc
                apiService.getTrips().enqueue(new Callback<List<Trip>>() {
                    @Override
                    public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            tripList.clear();
                            for (Trip t : response.body()) {
                                // Nếu tuyến xe của chuyến này nằm trong danh sách tuyến của nhà xe -> Hiển thị
                                if (myRouteIds.isEmpty() || myRouteIds.contains(t.getTuyenXeID())) {
                                    tripList.add(t);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            
                            if (tripList.isEmpty()) {
                                Log.w(TAG, "No trips found for routes: " + myRouteIds);
                            }
                        }
                    }
                    @Override public void onFailure(Call<List<Trip>> call, Throwable t) {
                        Toast.makeText(TripListActivity.this, "Lỗi kết nối chuyến xe", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override public void onFailure(Call<List<Route>> call, Throwable t) {
                Toast.makeText(TripListActivity.this, "Lỗi kết nối tuyến xe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String findVal(Map<String, Object> map, String... keys) {
        if (map == null) return "";
        for (String k : keys) {
            Object val = map.get(k);
            if (val != null) {
                if (val instanceof Map) {
                    // Nếu là Object, tìm tiếp ID bên trong
                    return findVal((Map<String, Object>) val, "NhaxeID", "TuyenXeID", "id");
                }
                return val.toString();
            }
        }
        return "";
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home_op_main).setOnClickListener(v -> startActivity(new Intent(this, OperatorMainActivity.class)));
        findViewById(R.id.nav_driver_op).setOnClickListener(v -> startActivity(new Intent(this, QLNhaxeActivity.class)));
        findViewById(R.id.nav_route_op).setOnClickListener(v -> startActivity(new Intent(this, QLTuyenxeActivity.class)));
        findViewById(R.id.nav_vehicle_op).setOnClickListener(v -> startActivity(new Intent(this, PhuongTienManagementActivity.class)));
        findViewById(R.id.nav_trip_op).setOnClickListener(v -> loadTripsByOperator());
    }

    @Override public void onEdit(Trip trip, int position) {
        Intent intent = new Intent(this, CreateTripActivity.class);
        intent.putExtra("editTrip", trip);
        startActivityForResult(intent, 101);
    }

    @Override public void onClick(Trip trip, int position) {
        Intent intent = new Intent(this, TripDetailActivity.class);
        intent.putExtra("trip", trip);
        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) loadTripsByOperator();
    }
}
