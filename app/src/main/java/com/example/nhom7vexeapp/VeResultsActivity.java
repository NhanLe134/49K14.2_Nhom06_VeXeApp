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
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ve_results);

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();

        Intent intent = getIntent();
        String origin = intent.getStringExtra("ORIGIN_KEY");
        String destination = intent.getStringExtra("DESTINATION_KEY");
        String searchDate = intent.getStringExtra("DATE_KEY");
        String searchTime = intent.getStringExtra("TIME_KEY");

        tvRouteTitle.setText(origin + " - " + destination);
        tvDateTitle.setText((searchDate == null || searchDate.isEmpty()) ? "Tất cả ngày" : searchDate);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchTripsFromServer(origin, destination, searchDate, searchTime);
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

    private void fetchTripsFromServer(String origin, String destination, String date, String time) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        tvNoResult.setVisibility(View.GONE);

        apiService.getChuyenXe().enqueue(new Callback<List<TripSearchResult>>() {
            @Override
            public void onResponse(Call<List<TripSearchResult>> call, Response<List<TripSearchResult>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<TripSearchResult> allTrips = response.body();
                    if (allTrips != null && !allTrips.isEmpty()) {
                        applySmartFilter(allTrips, origin, destination, date, time);
                    } else {
                        showNoResults();
                    }
                } else {
                    Log.e("API_ERROR", "Response Code: " + response.code());
                    Toast.makeText(VeResultsActivity.this, "Lỗi khi lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                    showNoResults();
                }
            }

            @Override
            public void onFailure(Call<List<TripSearchResult>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(VeResultsActivity.this, "Lỗi kết nối server!", Toast.LENGTH_LONG).show();
                showNoResults();
            }
        });
    }

    private void applySmartFilter(List<TripSearchResult> allTrips, String sOrigin, String sDest, String sDate, String sTime) {
        tripList.clear();
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        String nowStrDate = sdfDate.format(new Date());
        String nowStrTime = sdfTime.format(new Date());
        String formattedSearchDate = convertDateFormat(sDate);

        for (TripSearchResult trip : allTrips) {
            // Lọc trạng thái: Chỉ lấy những chuyến "Chưa hoàn thành"
            String status = trip.getStatus();
            if (status != null && !status.equalsIgnoreCase("Chưa hoàn thành")) {
                continue;
            }

            String tripDate = trip.getDate() != null ? trip.getDate() : "";
            String tripTime = trip.getTime() != null ? trip.getTime() : "00:00";

            // Lọc thời gian thực tế: Không hiển thị chuyến trong quá khứ
            if (tripDate.compareTo(nowStrDate) < 0) continue;
            if (tripDate.equals(nowStrDate) && tripTime.compareTo(nowStrTime) < 0) continue;

            boolean matchOrigin = sOrigin.equals("Tất cả") || (trip.getTuyenXeName() != null && trip.getTuyenXeName().contains(sOrigin));
            boolean matchDest = sDest.equals("Tất cả") || (trip.getTuyenXeName() != null && trip.getTuyenXeName().contains(sDest));
            boolean matchDate = (sDate == null || sDate.isEmpty()) || tripDate.equals(formattedSearchDate);

            boolean matchTime = true;
            if (sTime != null && !sTime.isEmpty()) {
                matchTime = isWithinThreeHours(tripTime, sTime);
            }

            if (matchOrigin && matchDest && matchDate && matchTime) {
                tripList.add(trip);
            }
        }

        adapter.notifyDataSetChanged();
        if (tripList.isEmpty()) {
            showNoResults();
        } else {
            tvNoResult.setVisibility(View.GONE);
        }
    }

    private void showNoResults() {
        if (tvNoResult != null) {
            tvNoResult.setVisibility(View.VISIBLE);
            tvNoResult.setText("Không tìm thấy chuyến phù hợp");
        }
    }

    private boolean isWithinThreeHours(String tripTime, String searchTime) {
        try {
            String[] tParts = tripTime.split(":");
            String[] sParts = searchTime.split(":");
            int tMin = Integer.parseInt(tParts[0]) * 60 + Integer.parseInt(tParts[1]);
            int sMin = Integer.parseInt(sParts[0]) * 60 + Integer.parseInt(sParts[1]);
            return Math.abs(tMin - sMin) <= 180;
        } catch (Exception e) { return true; }
    }

    private String convertDateFormat(String date) {
        if (date == null || date.isEmpty()) return "";
        try {
            String[] parts = date.split("/");
            if (parts.length == 3) return parts[2] + "-" + parts[1] + "-" + parts[0];
        } catch (Exception e) {}
        return date;
    }
}
