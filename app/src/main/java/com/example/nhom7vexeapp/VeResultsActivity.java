package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.adapters.VeResultAdapter;
import com.example.nhom7vexeapp.models.TripSearchResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VeResultsActivity extends AppCompatActivity {

    private RecyclerView rvVeResults;
    private VeResultAdapter adapter;
    private TextView tvRouteTitle, tvDateTitle, tvNoResult;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private List<TripSearchResult> tripList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ve_results);

        initViews();

        // Nhận dữ liệu tìm kiếm
        Intent intent = getIntent();
        String origin = intent.getStringExtra("ORIGIN_KEY");
        String destination = intent.getStringExtra("DESTINATION_KEY");
        String searchDate = intent.getStringExtra("DATE_KEY"); // Định dạng dd/MM/yyyy

        // Hiển thị thông tin tìm kiếm lên header
        tvRouteTitle.setText(origin + " - " + destination);
        tvDateTitle.setText(searchDate);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        
        // Gọi API lấy dữ liệu thật
        fetchTripsFromServer(searchDate);
    }

    private void initViews() {
        rvVeResults = findViewById(R.id.rvVeResults);
        tvRouteTitle = findViewById(R.id.tvRouteTitle);
        tvDateTitle = findViewById(R.id.tvDateTitle);
        tvNoResult = findViewById(R.id.tvNoResult); 
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar); 
    }

    private void setupRecyclerView() {
        rvVeResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VeResultAdapter(tripList, trip -> {
            Intent nextIntent = new Intent(VeResultsActivity.this, DatVeActivity.class);
            nextIntent.putExtra("selected_trip", trip);
            startActivity(nextIntent);
        });
        rvVeResults.setAdapter(adapter);
    }

    private void fetchTripsFromServer(String searchDate) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getApiService().getChuyenXe().enqueue(new Callback<List<TripSearchResult>>() {
            @Override
            public void onResponse(Call<List<TripSearchResult>> call, Response<List<TripSearchResult>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<TripSearchResult> allTrips = response.body();
                    filterAndDisplayTrips(allTrips, searchDate);
                } else {
                    Toast.makeText(VeResultsActivity.this, "Không thể lấy dữ liệu chuyến xe!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TripSearchResult>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Log.e("API_ERROR", "Fetch trips failed: " + t.getMessage());
                Toast.makeText(VeResultsActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplayTrips(List<TripSearchResult> allTrips, String searchDate) {
        tripList.clear();
        
        // Định dạng ngày từ API thường là yyyy-MM-dd
        String formattedSearchDate = convertDateFormat(searchDate);

        for (TripSearchResult trip : allTrips) {
            if (trip.getDate() != null && trip.getDate().equals(formattedSearchDate)) {
                tripList.add(trip);
            }
        }

        adapter.notifyDataSetChanged();

        if (tripList.isEmpty()) {
            if (tvNoResult != null) tvNoResult.setVisibility(View.VISIBLE);
        } else {
            if (tvNoResult != null) tvNoResult.setVisibility(View.GONE);
        }
    }

    private String convertDateFormat(String date) {
        if (date == null) return "";
        try {
            // Chuyển từ dd/MM/yyyy sang yyyy-MM-dd
            String[] parts = date.split("/");
            if (parts.length == 3) {
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            }
        } catch (Exception e) {
            return date;
        }
        return date;
    }
}
