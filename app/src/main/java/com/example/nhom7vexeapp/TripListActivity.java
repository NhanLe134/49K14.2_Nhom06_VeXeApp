package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom7vexeapp.adapters.TripAdapter;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Trip;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripListActivity extends AppCompatActivity implements TripAdapter.OnTripActionListener {
    private RecyclerView rvTrips;
    private TripAdapter adapter;
    private List<Trip> tripList = new ArrayList<>();
    private ApiService apiService;
    private ImageView imgOpProfile;
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
                Intent intent = new Intent(TripListActivity.this, CreateTripActivity.class);
                startActivityForResult(intent, 100);
            });
        }

        imgOpProfile = findViewById(R.id.imgOpProfile);
        if (imgOpProfile != null) {
            imgOpProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorProfileActivity.class));
            });
        }

        ImageView imgLogo = findViewById(R.id.imgLogo);
        if (imgLogo != null) {
            imgLogo.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorMainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            });
        }

        setupBottomNavigation();
    }

    private void loadTripsByOperator() {
        // 1. TẢI DANH SÁCH TUYẾN XE TRƯỚC ĐỂ BIẾT TUYẾN NÀO THUỘC NHÀ XE NÀO
        apiService.getRoutes().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> resR) {
                List<String> myRouteIds = new ArrayList<>();
                if (resR.isSuccessful() && resR.body() != null) {
                    for (Map<String, Object> r : resR.body()) {
                        String nxe = String.valueOf(r.get("Nhaxe"));
                        if (nxe.equals(opUid)) {
                            myRouteIds.add(String.valueOf(r.get("TuyenXeID")));
                        }
                    }
                }

                // 2. TẢI CHUYẾN XE VÀ CHỈ LẤY NHỮNG CHUYẾN THUỘC TUYẾN CỦA NHÀ XE MÌNH
                apiService.getTrips().enqueue(new Callback<List<Trip>>() {
                    @Override
                    public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            tripList.clear();
                            for (Trip t : response.body()) {
                                if (myRouteIds.contains(t.getTuyenXeID())) {
                                    tripList.add(t);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override public void onFailure(Call<List<Trip>> call, Throwable t) {}
                });
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorMainActivity.class));
                finish();
            });
        }
        findViewById(R.id.nav_driver_op).setOnClickListener(v -> { startActivity(new Intent(this, QLNhaxeActivity.class)); finish(); });
        findViewById(R.id.nav_route_op).setOnClickListener(v -> { startActivity(new Intent(this, QLTuyenxeActivity.class)); finish(); });
        findViewById(R.id.nav_vehicle_op).setOnClickListener(v -> { startActivity(new Intent(this, PhuongTienManagementActivity.class)); finish(); });
    }

    @Override
    public void onEdit(Trip trip, int position) {
        Intent intent = new Intent(this, CreateTripActivity.class);
        intent.putExtra("editTrip", trip);
        intent.putExtra("position", position);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onClick(Trip trip, int position) {
        Intent intent = new Intent(this, TripDetailActivity.class);
        intent.putExtra("trip", trip);
        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadTripsByOperator();
        }
    }
}
