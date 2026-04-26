package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Trip;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.MapEventsOverlay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripRouteActivity extends AppCompatActivity {

    private MapView map;
    private LinearLayout layoutPoints;
    private TextView tvDurationInfo, tvTitleList;
    private Trip currentTrip;
    private boolean isShowingPickup = true;
    private List<Map<String, Object>> tickets = new ArrayList<>();
    private Map<String, String> customerNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        setContentView(R.layout.activity_trip_route);

        initViews();
        currentTrip = (Trip) getIntent().getSerializableExtra("trip_data");

        if (currentTrip != null) {
            loadDurationFromRoute();
            fetchData();
        }

        setupBottomNavigation();
    }

    private void initViews() {
        map = findViewById(R.id.map);
        layoutPoints = findViewById(R.id.layoutPickupPoints);
        tvDurationInfo = findViewById(R.id.tvDurationInfo);
        tvTitleList = findViewById(R.id.tvTitleList);
        
        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
        }
    }

    private void toggleViewMode() {
        isShowingPickup = !isShowingPickup;
        tvTitleList.setText(isShowingPickup ? "Danh sách điểm đón" : "Danh sách điểm trả");
        updateListAndMarkers();
    }

    private void loadDurationFromRoute() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getRoutes().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> route : response.body()) {
                        String id = findVal(route, "TuyenXeID", "id");
                        if (id.equalsIgnoreCase(currentTrip.getTuyenXeID())) {
                            String time = findVal(route, "ThoiGian", "duration");
                            tvDurationInfo.setText("Thời gian dự kiến: " + (time.isEmpty() || time.equals("null") ? "2h" : time));
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void fetchData() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getKhachHangList().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> resK) {
                if (resK.isSuccessful() && resK.body() != null) {
                    for (Map<String, Object> kh : resK.body()) {
                        String id = findVal(kh, "KhachHangID", "id");
                        String name = findVal(kh, "Hovaten", "TenKhachHang");
                        if (!id.isEmpty()) customerNames.put(id, name);
                    }
                }
                fetchTickets();
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) { fetchTickets(); }
        });
    }

    private void fetchTickets() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getTicketsByTrip(currentTrip.getId()).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tickets = response.body();
                    updateListAndMarkers();
                }
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void updateListAndMarkers() {
        if (map == null) return;
        map.getOverlays().clear();
        layoutPoints.removeAllViews();

        // 1. Lắng nghe click bản đồ
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override public boolean singleTapConfirmedHelper(GeoPoint p) { toggleViewMode(); return true; }
            @Override public boolean longPressHelper(GeoPoint p) { return false; }
        };
        map.getOverlays().add(new MapEventsOverlay(mReceive));

        // 2. Tọa độ Đà Nẵng - Huế
        GeoPoint startPoint = new GeoPoint(16.0544, 108.2022); // Đà Nẵng
        GeoPoint endPoint = new GeoPoint(16.4637, 107.5909);   // Huế

        // 3. Vẽ Polyline phong cách Figma
        Polyline line = new Polyline();
        line.setColor(Color.parseColor("#03A9F4")); // Xanh sáng giống Figma
        line.setWidth(10f);
        line.addPoint(startPoint);

        // 4. Tạo Marker (dạng Point) và Item danh sách
        for (int i = 0; i < tickets.size(); i++) {
            Map<String, Object> v = tickets.get(i);
            
            // Giả lập vị trí dọc đường đi
            double ratio = (double)(i + 1) / (tickets.size() + 1);
            double lat = startPoint.getLatitude() + (endPoint.getLatitude() - startPoint.getLatitude()) * ratio;
            double lon = startPoint.getLongitude() + (endPoint.getLongitude() - startPoint.getLongitude()) * ratio;
            
            GeoPoint point = new GeoPoint(lat + (Math.random()-0.5)*0.008, lon + (Math.random()-0.5)*0.008);
            line.addPoint(point);

            // Tạo icon Point (Chấm tròn trắng viền xanh)
            Marker m = new Marker(map);
            m.setPosition(point);
            
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.WHITE);
            circle.setStroke(5, Color.parseColor("#03A9F4"));
            circle.setSize(20, 20);
            
            m.setIcon(circle);
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            
            String khId = findVal(v, "KhachHang", "khachhang");
            String name = customerNames.getOrDefault(khId, "Khách hàng");
            m.setTitle(name + (isShowingPickup ? " - Đón" : " - Trả"));
            map.getOverlays().add(m);

            updatePassengerItem(v, name);
        }

        line.addPoint(endPoint);
        map.getOverlays().add(line);

        // 5. Thêm Point điểm đầu và cuối to hơn
        addPointMarker(startPoint, "Điểm đi");
        addPointMarker(endPoint, "Điểm đến");

        // 6. Thay đổi góc nhìn Map
        if (isShowingPickup) {
            map.getController().setZoom(15.0);
            map.getController().animateTo(startPoint);
        } else {
            map.getController().setZoom(15.0);
            map.getController().animateTo(endPoint);
        }

        map.invalidate();
    }

    private void addPointMarker(GeoPoint point, String title) {
        Marker m = new Marker(map);
        m.setPosition(point);
        m.setTitle(title);
        
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor("#03A9F4"));
        circle.setSize(35, 35);
        
        m.setIcon(circle);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        map.getOverlays().add(m);
    }

    private void updatePassengerItem(Map<String, Object> v, String name) {
        String phone = findVal(v, "SoDienThoai", "phone");
        String pickup = findVal(v, "DiemDon", "pickup");
        String dropoff = findVal(v, "DiemTra", "dropoff");
        String fullGhe = findVal(v, "Ghe", "MaGhe");
        
        String displayGhe = fullGhe;
        if (fullGhe.startsWith(currentTrip.getId())) {
            displayGhe = fullGhe.substring(currentTrip.getId().length());
        }

        View view = getLayoutInflater().inflate(R.layout.item_passenger, null);
        ((TextView)view.findViewById(R.id.tvPassengerName)).setText(name);
        ((TextView)view.findViewById(R.id.tvPassengerPhone)).setText(phone);
        
        TextView tvP = view.findViewById(R.id.tvPickup);
        TextView tvD = view.findViewById(R.id.tvDropoff);

        if (isShowingPickup) {
            tvP.setVisibility(View.VISIBLE);
            tvP.setText("Điểm đón: " + pickup);
            tvD.setVisibility(View.GONE);
        } else {
            tvP.setVisibility(View.GONE);
            tvD.setVisibility(View.VISIBLE);
            tvD.setText("Điểm trả: " + dropoff);
        }
        
        ((TextView)view.findViewById(R.id.tvSeatNumber)).setText(displayGhe);
        layoutPoints.addView(view);
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

    private void setupBottomNavigation() {
        findViewById(R.id.nav_home_op_main).setOnClickListener(v -> { startActivity(new Intent(this, OperatorMainActivity.class)); finish(); });
        findViewById(R.id.nav_trip_op).setOnClickListener(v -> { startActivity(new Intent(this, TripListActivity.class)); finish(); });
        findViewById(R.id.nav_driver_op).setOnClickListener(v -> { startActivity(new Intent(this, QLNhaxeActivity.class)); finish(); });
        findViewById(R.id.nav_route_op).setOnClickListener(v -> { startActivity(new Intent(this, QLTuyenxeActivity.class)); finish(); });
        findViewById(R.id.nav_vehicle_op).setOnClickListener(v -> { startActivity(new Intent(this, PhuongTienManagementActivity.class)); finish(); });
    }

    @Override public void onResume() { super.onResume(); if (map != null) map.onResume(); }
    @Override public void onPause() { super.onPause(); if (map != null) map.onPause(); }
}
