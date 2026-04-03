package com.example.nhom7vexeapp;

import android.content.Intent;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripListActivity extends AppCompatActivity implements TripAdapter.OnTripActionListener {
    private RecyclerView rvTrips;
    private TripAdapter adapter;
    private List<Trip> tripList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        // Khởi tạo API Service
        apiService = ApiClient.getClient().create(ApiService.class);

        rvTrips = findViewById(R.id.rvTrips);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        
        // Khởi tạo adapter với danh sách trống ban đầu
        adapter = new TripAdapter(tripList, this);
        rvTrips.setAdapter(adapter);

        // Tải dữ liệu thật từ Django
        loadTripsFromApi();

        Button btnCreateTrip = findViewById(R.id.btnCreateTrip);
        if (btnCreateTrip != null) {
            btnCreateTrip.setOnClickListener(v -> {
                Intent intent = new Intent(TripListActivity.this, CreateTripActivity.class);
                startActivityForResult(intent, 100);
            });
        }

        ImageView imgLogo = findViewById(R.id.imgLogo);
        if (imgLogo != null) {
            imgLogo.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        setupBottomNavigation();
    }

    private void loadTripsFromApi() {
        Toast.makeText(this, "Đang tải chuyến xe...", Toast.LENGTH_SHORT).show();
        
        apiService.getTrips().enqueue(new Callback<List<Trip>>() {
            @Override
            public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tripList.clear();
                    tripList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    if (tripList.isEmpty()) {
                        Toast.makeText(TripListActivity.this, "Chưa có chuyến xe nào trên hệ thống", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(TripListActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Trip>> call, Throwable t) {
                Log.e("API_ERROR", "Fetch trips failed", t);
                Toast.makeText(TripListActivity.this, "Không thể kết nối server Django!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        // ... giữ nguyên các xử lý navigation khác
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
        try {
            Intent intent = new Intent(this, TripDetailActivity.class);
            intent.putExtra("trip", trip);
            intent.putExtra("position", position);
            startActivityForResult(intent, 102);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Sau khi thêm hoặc sửa, load lại từ API cho đồng bộ nhất
            loadTripsFromApi();
        }
    }
}
