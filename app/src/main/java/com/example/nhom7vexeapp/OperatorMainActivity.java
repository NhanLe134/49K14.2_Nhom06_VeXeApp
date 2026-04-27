package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.NhaXe;
import com.example.nhom7vexeapp.models.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorMainActivity extends AppCompatActivity {

    private TextView tvHeaderName, tvBannerName, tvBannerId;
    private TableLayout tlSchedule;
    private String opUid;
    private ApiService apiService;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_main);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        opUid = pref.getString("op_uid", "NX00001");

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        loadNhaxeInfo();
        loadRealSchedule();
        setupBottomNavigation();
        setupProfileClick();
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tvHeaderNhaxeName);
        tvBannerName = findViewById(R.id.tvBannerOpName);
        tvBannerId = findViewById(R.id.tvBannerOpId);
        tlSchedule = findViewById(R.id.tlSchedule);
    }

    private void loadNhaxeInfo() {
        apiService.getNhaXeDetail(opUid).enqueue(new Callback<NhaXe>() {
            @Override
            public void onResponse(Call<NhaXe> call, Response<NhaXe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NhaXe data = response.body();
                    String name = data.getBusName();
                    if (tvHeaderName != null) tvHeaderName.setText(name);
                    if (tvBannerName != null) tvBannerName.setText(name);
                    if (tvBannerId != null) tvBannerId.setText("Mã: " + opUid);
                }
            }
            @Override public void onFailure(Call<NhaXe> call, Throwable t) {}
        });
    }

    private void loadRealSchedule() {
        apiService.getChiTietTaiXe().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                List<Map<String, Object>> myDrivers = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> d : response.body()) {
                        String nxe = findValueInMap(d, "Nhaxe", "nhaxe", "NhaxeID");
                        if (nxe.isEmpty() || nxe.equalsIgnoreCase(opUid)) myDrivers.add(d);
                    }
                }
                fetchTripsAndBuildTable(myDrivers);
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) { fetchTripsAndBuildTable(new ArrayList<>()); }
        });
    }

    private void fetchTripsAndBuildTable(List<Map<String, Object>> drivers) {
        apiService.getTrips().enqueue(new Callback<List<Trip>>() {
            @Override
            public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                List<Trip> trips = (response.isSuccessful() && response.body() != null) ? response.body() : new ArrayList<>();
                buildScheduleTable(drivers, trips);
            }
            @Override public void onFailure(Call<List<Trip>> call, Throwable t) { buildScheduleTable(drivers, new ArrayList<>()); }
        });
    }

    private void buildScheduleTable(List<Map<String, Object>> drivers, List<Trip> allTrips) {
        if (tlSchedule == null) return;
        tlSchedule.removeAllViews();

        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(Color.parseColor("#F5F5F5"));
        headerRow.addView(createHeaderText("Tài xế"));

        Calendar cal = Calendar.getInstance();
        String[] dates = new String[3];
        for (int i = 0; i < 3; i++) {
            dates[i] = sdf.format(cal.getTime());
            headerRow.addView(createHeaderText(displaySdf.format(cal.getTime())));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        tlSchedule.addView(headerRow);

        for (Map<String, Object> driverMap : drivers) {
            String driverId = findValueInMap(driverMap, "Taixe", "TaiXe", "id");
            String driverName = findValueInMap(driverMap, "HoTen", "hoten");

            TableRow row = new TableRow(this);
            row.setBackgroundColor(Color.WHITE);

            TextView tvName = new TextView(this);
            tvName.setText(driverName.isEmpty() ? driverId : driverName);
            tvName.setPadding(15, 40, 15, 40);
            tvName.setGravity(Gravity.CENTER);
            tvName.setTextColor(Color.BLACK);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextSize(12);
            row.addView(tvName);

            for (String dateStr : dates) {
                LinearLayout cell = new LinearLayout(this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setPadding(5, 10, 5, 10);
                cell.setGravity(Gravity.CENTER);

                for (Trip trip : allTrips) {
                    if (trip.getTaiXeID() != null && trip.getTaiXeID().equalsIgnoreCase(driverId) && trip.getDate().startsWith(dateStr)) {
                        View item = getLayoutInflater().inflate(R.layout.item_schedule_trip, cell, false);
                        ((TextView)item.findViewById(R.id.tvScheduleTime)).setText(trip.getTime());
                        ((TextView)item.findViewById(R.id.tvScheduleRoute)).setText(trip.getRouteName().replace("Tuyến: ", ""));

                        item.setOnClickListener(v -> {
                            Intent intent = new Intent(OperatorMainActivity.this, TripDetailActivity.class);
                            intent.putExtra("trip", trip);
                            startActivity(intent);
                        });

                        cell.addView(item);
                    }
                }
                if (cell.getChildCount() == 0) {
                    TextView tvOff = new TextView(this);
                    tvOff.setText("Nghỉ");
                    tvOff.setTextSize(10);
                    tvOff.setTextColor(Color.LTGRAY);
                    cell.addView(tvOff);
                }
                row.addView(cell);
            }
            tlSchedule.addView(row);
        }
    }

    private String findValueInMap(Map<String, Object> map, String... keys) {
        if (map == null) return "";
        for (String key : keys) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key) && entry.getValue() != null) return entry.getValue().toString();
            }
        }
        return "";
    }

    private TextView createHeaderText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(5, 25, 5, 25);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(12);
        return tv;
    }

    private void setupProfileClick() {
        View p = findViewById(R.id.imgOpProfile);
        if (p != null) p.setOnClickListener(v -> startActivity(new Intent(this, OperatorProfileActivity.class)));
    }

    private void setupBottomNavigation() {
        // TRANG CHỦ
        View navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) navHome.setOnClickListener(v -> {
            // Đã ở trang chủ rồi không cần chuyển
        });

        // TÀI XẾ
        View navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) navDriver.setOnClickListener(v -> {
            startActivity(new Intent(this, DriverSelectionActivity.class));
        });

        // CHUYẾN XE
        View navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) navTrip.setOnClickListener(v -> {
            startActivity(new Intent(this, TripListActivity.class));
        });

        // TUYẾN XE
        View navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) navRoute.setOnClickListener(v -> {
            startActivity(new Intent(this, QLTuyenxeActivity.class));
        });

        // PHƯƠNG TIỆN (Chuyển đến màn hình quản lý chung thay vì danh sách)
        View navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) navVehicle.setOnClickListener(v -> {
            startActivity(new Intent(this, PhuongTienManagementActivity.class));
        });
    }
}
