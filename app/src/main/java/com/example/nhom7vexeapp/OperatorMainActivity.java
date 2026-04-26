package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
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

    private static final String TAG = "DEBUG_OP_MAIN";
    private TextView tvHeaderName, tvBannerName, tvBannerId;
    private TableLayout tlSchedule;
    private String opUid;
    private ApiService apiService;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_operator_main);
            
            SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            opUid = pref.getString("op_uid", "NX00001");

            apiService = ApiClient.getClient().create(ApiService.class);
            
            initViews();
            loadNhaxeInfo();
            loadRealSchedule();
            setupBottomNavigation();
            setupProfileClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tvHeaderNhaxeName);
        tvBannerName = findViewById(R.id.tvBannerOpName);
        tvBannerId = findViewById(R.id.tvBannerOpId);
        tlSchedule = findViewById(R.id.tlSchedule);
    }

    private void loadNhaxeInfo() {
        apiService.getNhaXeDetail(opUid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    String name = findValueInMap(data, "Tennhaxe", "TenNhaXe");
                    if (tvHeaderName != null) tvHeaderName.setText(name);
                    if (tvBannerName != null) tvBannerName.setText(name);
                    if (tvBannerId != null) tvBannerId.setText("Mã: " + opUid);
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void loadRealSchedule() {
        // ✅ LẤY DỮ LIỆU TỪ BẢNG CHI TIẾT TÀI XẾ ĐỂ CÓ HỌ TÊN
        apiService.getChiTietTaiXe().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                List<Map<String, Object>> myDrivers = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> d : response.body()) {
                        String nxe = findValueInMap(d, "Nhaxe", "nhaxe", "NhaxeID");
                        if (nxe.isEmpty() || nxe.equalsIgnoreCase(opUid)) {
                            myDrivers.add(d);
                        }
                    }
                }
                fetchTripsAndBuildTable(myDrivers);
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                fetchTripsAndBuildTable(new ArrayList<>());
            }
        });
    }

    private void fetchTripsAndBuildTable(List<Map<String, Object>> drivers) {
        apiService.getTrips().enqueue(new Callback<List<Trip>>() {
            @Override
            public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                List<Trip> trips = (response.isSuccessful() && response.body() != null) ? response.body() : new ArrayList<>();
                buildScheduleTable(drivers, trips);
            }
            @Override public void onFailure(Call<List<Trip>> call, Throwable t) {
                buildScheduleTable(drivers, new ArrayList<>());
            }
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
            // ✅ LẤY ID VÀ HỌ TÊN CHUẨN TỪ BẢNG CHITIETTAIXE
            // driverId dùng để so khớp với chuyến xe (Ví dụ: TAI0002)
            String driverId = findValueInMap(driverMap, "Taixe", "TaiXe", "TaiXeID", "id");
            // driverName dùng để hiển thị lên bảng (Ví dụ: Đặng Hay)
            String driverName = findValueInMap(driverMap, "HoTen", "HOTEN", "Ho_Ten", "hoten", "name");

            TableRow row = new TableRow(this);
            row.setBackgroundColor(Color.WHITE);

            TextView tvName = new TextView(this);
            // HIỂN THỊ: Ưu tiên Họ tên (driverName), nếu rỗng mới hiện ID (driverId)
            tvName.setText(driverName.isEmpty() ? (driverId.isEmpty() ? "Tài xế" : driverId) : driverName);
            tvName.setPadding(15, 40, 15, 40);
            tvName.setGravity(Gravity.CENTER);
            tvName.setTextColor(Color.BLACK);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextSize(13);
            row.addView(tvName);

            for (String dateStr : dates) {
                LinearLayout cell = new LinearLayout(this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setPadding(5, 10, 5, 10);
                cell.setGravity(Gravity.CENTER);

                boolean hasTrip = false;
                for (Trip trip : allTrips) {
                    if (trip.getTaiXeID() != null && !driverId.isEmpty() &&
                            trip.getTaiXeID().equalsIgnoreCase(driverId) &&
                            trip.getDate().startsWith(dateStr)) {

                        hasTrip = true;
                        try {
                            View item = getLayoutInflater().inflate(R.layout.item_schedule_trip, cell, false);
                            ((TextView)item.findViewById(R.id.tvScheduleTime)).setText(trip.getTime());
                            ((TextView)item.findViewById(R.id.tvScheduleRoute)).setText(trip.getRouteName().replace("Tuyến: ", ""));
                            cell.addView(item);
                        } catch (Exception e) {}
                    }
                }

                if (!hasTrip) {
                    TextView tvOff = new TextView(this);
                    tvOff.setText("Nghỉ");
                    tvOff.setTextSize(11);
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
        return tv;
    }

    private void setupProfileClick() {
        View profileBtn = findViewById(R.id.imgOpProfile);
        if (profileBtn != null) profileBtn.setOnClickListener(v -> startActivity(new Intent(this, OperatorProfileActivity.class)));
    }

    private void setupBottomNavigation() {
        View btnDriver = findViewById(R.id.nav_driver_op);
        if (btnDriver != null) btnDriver.setOnClickListener(v -> startActivity(new Intent(this, QLNhaxeActivity.class)));
        View btnTrip = findViewById(R.id.nav_trip_op);
        if (btnTrip != null) btnTrip.setOnClickListener(v -> startActivity(new Intent(this, TripListActivity.class)));
        View btnRoute = findViewById(R.id.nav_route_op);
        if (btnRoute != null) btnRoute.setOnClickListener(v -> startActivity(new Intent(this, QLTuyenxeActivity.class)));
        View btnVehicle = findViewById(R.id.nav_vehicle_op);
        if (btnVehicle != null) btnVehicle.setOnClickListener(v -> startActivity(new Intent(this, PhuongTienManagementActivity.class)));
    }
}
