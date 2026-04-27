package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.adapters.RouteAdapter;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Route;
import com.google.android.material.button.MaterialButton;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// #Trang
public class QLTuyenxeActivity extends AppCompatActivity implements RouteAdapter.OnRouteActionListener {

    private RecyclerView rvRoutes;
    private RouteAdapter adapter;
    private List<Route> routeList;
    private MaterialButton btnAddRoute;
    private ImageView btnBack;
    private TextView tvToolbarTitle;

    private String opUid;
    private ApiService apiService;

    private CardView inlineFormCard;
    private TextView tvFormGuide;
    private EditText edtRouteName, edtStartPoint, edtMidPoint, edtEndPoint;
    private EditText edtAutoDistance, edtAutoTime;
    private TextView tvErrorRouteName, tvErrorStartPoint, tvErrorEndPoint;
    private MaterialButton btnSaveForm, btnCancelForm;

    private Route editingRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_tuyenxe);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        opUid = pref.getString("op_uid", "NX00001");
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupRecyclerView();
        fetchRoutesFromApi();
        setupEvents();
        setupNavigation();
    }

    private void initViews() {
        rvRoutes = findViewById(R.id.rvRoutes);
        btnAddRoute = findViewById(R.id.btnAddRoute);
        btnBack = findViewById(R.id.btnBack);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        inlineFormCard = findViewById(R.id.inlineFormCard);
        tvFormGuide = findViewById(R.id.tvFormGuide);
        edtRouteName = findViewById(R.id.edtRouteName);
        edtStartPoint = findViewById(R.id.edtStartPoint);
        edtMidPoint = findViewById(R.id.edtMidPoint);
        edtEndPoint = findViewById(R.id.edtEndPoint);
        tvErrorRouteName = findViewById(R.id.tvErrorRouteName);
        tvErrorStartPoint = findViewById(R.id.tvErrorStartPoint);
        tvErrorEndPoint = findViewById(R.id.tvErrorEndPoint);
        btnSaveForm = findViewById(R.id.btnSaveForm);
        btnCancelForm = findViewById(R.id.btnCancelForm);
        edtAutoDistance = findViewById(R.id.edtAutoDistance);
        edtAutoTime = findViewById(R.id.edtAutoTime);
    }

    private void setupRecyclerView() {
        routeList = new ArrayList<>();
        adapter = new RouteAdapter(routeList, this);
        rvRoutes.setLayoutManager(new LinearLayoutManager(this));
        rvRoutes.setAdapter(adapter);
    }

    private void fetchRoutesFromApi() {
        if (opUid == null || opUid.isEmpty()) return;
        apiService.getRoutesRaw().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    routeList.clear();
                    for (Map<String, Object> rMap : response.body()) {
                        String nhaXeId = findVal(rMap, "Nhaxe", "NhaxeID", "nhaXe");
                        if (opUid.equals(nhaXeId)) {
                            String id = findVal(rMap, "TuyenXeID", "id", "tuyenXeID");
                            String name = findVal(rMap, "TenTuyen", "name", "tenTuyen");
                            String start = findVal(rMap, "DiemDi", "startPoint", "diemDi");
                            String mid = findVal(rMap, "DiemTrungGian", "midPoint");
                            String end = findVal(rMap, "DiemDen", "endPoint", "diemDen");
                            
                            String dist = unifyDistance(findVal(rMap, "QuangDuong", "distance"));
                            String time = unifyTime(findVal(rMap, "ThoiGian", "time"));
                            
                            String status = findVal(rMap, "TrangThai", "status");

                            routeList.add(new Route(id, name, start, mid, end, dist, time, status));
                        }
                    }
                    sortAndNotify();
                }
            }

            private String findVal(Map<String, Object> m, String... keys) {
                for (String k : keys) {
                    if (m.containsKey(k) && m.get(k) != null) return m.get(k).toString();
                }
                return "";
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private String unifyDistance(String val) {
        if (val == null || val.isEmpty()) return "";
        String n = val.replaceAll("[^0-9,.]", "").replace(",", ".").trim();
        if (n.isEmpty()) return val;
        return n + " km";
    }

    private String unifyTime(String val) {
        if (val == null || val.isEmpty()) return "";
        if (val.contains("h")) {
            String h = val.split("h")[0].trim();
            return h + " giờ";
        }
        String n = val.replaceAll("[^0-9,.]", "").replace(".", ",").trim();
        if (n.isEmpty()) return val;
        return n + " giờ";
    }

    private void sortAndNotify() {
        Collections.sort(routeList, (r1, r2) -> getStatusPriority(r1.getStatus()) - getStatusPriority(r2.getStatus()));
        adapter.notifyDataSetChanged();
    }

    private int getStatusPriority(String status) {
        if (status == null) return 0;
        if (status.equals("Đang hoạt động")) return 0;
        if (status.equals("Bảo trì")) return 1;
        if (status.equals("Ngưng hoạt động")) return 2;
        return 3;
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (inlineFormCard.getVisibility() == View.VISIBLE) {
                showCancelConfirmationDialog(this::hideRouteForm);
            } else backToHome();
        });

        btnAddRoute.setOnClickListener(v -> showRouteForm(null));
        btnCancelForm.setOnClickListener(v -> {
            showCancelConfirmationDialog(this::hideRouteForm);
        });
        btnSaveForm.setOnClickListener(v -> validateAndSave());

        TextWatcher autoCalculateWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { autoCalculateOSM(); }
        };
        edtStartPoint.addTextChangedListener(autoCalculateWatcher);
        edtMidPoint.addTextChangedListener(autoCalculateWatcher);
        edtEndPoint.addTextChangedListener(autoCalculateWatcher);
    }

    private void autoCalculateOSM() {
        String start = deAccent(edtStartPoint.getText().toString());
        String mid = deAccent(edtMidPoint.getText().toString());
        String end = deAccent(edtEndPoint.getText().toString());

        if (start.isEmpty() || end.isEmpty()) {
            if (editingRoute == null) {
                edtAutoDistance.setText("tự động");
                edtAutoDistance.setTextColor(Color.GRAY);
                edtAutoTime.setText("tự động");
                edtAutoTime.setTextColor(Color.GRAY);
            }
            return;
        }

        int totalDist = 0;
        float totalTime = 0;

        if (!mid.isEmpty()) {
            int d1 = getBaseDist(start, mid);
            float t1 = getBaseTime(start, mid);
            int d2 = getBaseDist(mid, end);
            float t2 = getBaseTime(mid, end);
            if (d1 > 0 && d2 > 0) { totalDist = d1 + d2; totalTime = t1 + t2; }
        }
        
        if (totalDist == 0) {
            totalDist = getBaseDist(start, end);
            totalTime = getBaseTime(start, end);
        }

        if (totalDist == 0) {
            totalDist = (start.length() + end.length() + mid.length()) * 8 + 20;
            totalTime = (float) totalDist / 50 + 0.5f;
        }

        edtAutoDistance.setText(totalDist + " km");
        edtAutoDistance.setTextColor(Color.BLACK);
        String timeStr = String.format("%.1f", totalTime).replace(",", ".");
        edtAutoTime.setText(timeStr + " giờ");
        edtAutoTime.setTextColor(Color.BLACK);
    }

    private String deAccent(String str) {
        if (str == null) return "";
        String nfdNormalizedString = Normalizer.normalize(str.trim().toLowerCase(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replace('đ', 'd');
    }

    private int getBaseDist(String s, String e) {
        if ((s.contains("da nang") && e.contains("hue")) || (s.contains("hue") && e.contains("da nang"))) return 100;
        if ((s.contains("da nang") && e.contains("hoi an")) || (s.contains("hoi an") && e.contains("da nang"))) return 30;
        if ((s.contains("hue") && e.contains("hoi an")) || (s.contains("hoi an") && e.contains("hue"))) return 130;
        return 0;
    }

    private float getBaseTime(String s, String e) {
        if ((s.contains("da nang") && e.contains("hue")) || (s.contains("hue") && e.contains("da nang"))) return 2.5f;
        if ((s.contains("da nang") && e.contains("hoi an")) || (s.contains("hoi an") && e.contains("da nang"))) return 1.0f;
        if ((s.contains("hue") && e.contains("hoi an")) || (s.contains("hoi an") && e.contains("hue"))) return 3.5f;
        return 0;
    }

    private void backToHome() {
        Intent intent = new Intent(this, OperatorMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) navHome.setOnClickListener(v -> backToHome());
    }

    private void showCancelConfirmationDialog(Runnable onConfirm) {
        View dv = getLayoutInflater().inflate(R.layout.dialog_confirm_cancel, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvMsg = dv.findViewById(R.id.tvDialogMessage);
        if (tvMsg != null) tvMsg.setText("Bạn có thông tin chỉnh sửa chưa lưu,\nxác nhận hủy?");

        dv.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());
        dv.findViewById(R.id.btnYes).setOnClickListener(v -> {
            dialog.dismiss();
            onConfirm.run();
        });

        dialog.show();
    }

    private void showRouteForm(Route route) {
        editingRoute = route;
        rvRoutes.setVisibility(View.GONE);
        btnAddRoute.setVisibility(View.GONE);
        inlineFormCard.setVisibility(View.VISIBLE);
        clearErrors();
        tvFormGuide.setText("Vui lòng nhập thông tin tuyến xe. Các trường có dấu (*) là bắt buộc.");
        if (route == null) {
            tvToolbarTitle.setText("Thêm Tuyến xe");
            clearForm();
        } else {
            tvToolbarTitle.setText("Sửa thông tin Tuyến xe");
            edtRouteName.setText(route.getName());
            edtStartPoint.setText(route.getStartPoint());
            edtMidPoint.setText(route.getMidPoint());
            edtEndPoint.setText(route.getEndPoint());
            
            String dist = route.getDistance().replaceAll("[^0-9,.]", "").replace(",", ".");
            edtAutoDistance.setText(dist + " km");
            edtAutoDistance.setTextColor(Color.BLACK);
            
            String time = route.getTime().replaceAll("[^0-9,.]", "").replace(",", ".");
            edtAutoTime.setText(time + " giờ");
            edtAutoTime.setTextColor(Color.BLACK);
        }
    }

    private void hideRouteForm() {
        inlineFormCard.setVisibility(View.GONE);
        rvRoutes.setVisibility(View.VISIBLE);
        btnAddRoute.setVisibility(View.VISIBLE);
        tvToolbarTitle.setText("Quản lý tuyến xe");
        editingRoute = null;
    }

    private void clearForm() {
        edtRouteName.setText(""); edtStartPoint.setText(""); edtMidPoint.setText(""); edtEndPoint.setText("");
        edtAutoDistance.setText("tự động"); edtAutoDistance.setTextColor(Color.GRAY);
        edtAutoTime.setText("tự động"); edtAutoTime.setTextColor(Color.GRAY);
    }

    private void validateAndSave() {
        clearErrors();
        boolean isValid = true;
        String name = edtRouteName.getText().toString().trim();
        String start = edtStartPoint.getText().toString().trim();
        String end = edtEndPoint.getText().toString().trim();
        String distStr = edtAutoDistance.getText().toString().trim();
        String timeStr = edtAutoTime.getText().toString().trim();

        if (name.isEmpty()) { 
            showFieldError(edtRouteName, tvErrorRouteName, "Vui lòng nhập tên tuyến xe."); 
            isValid = false; 
        }

        if (start.isEmpty()) { 
            showFieldError(edtStartPoint, tvErrorStartPoint, "Vui lòng nhập điểm đi."); 
            isValid = false; 
        }

        if (end.isEmpty()) { 
            showFieldError(edtEndPoint, tvErrorEndPoint, "Vui lòng nhập điểm đến."); 
            isValid = false; 
        }

        if (!isValid) return;

        Map<String, String> data = new HashMap<>();
        data.put("tenTuyen", name); data.put("diemDi", start); data.put("DiemTrungGian", edtMidPoint.getText().toString());
        data.put("diemDen", end); 
        
        String finalDist = distStr;
        if (finalDist.isEmpty() || finalDist.equals("tự động")) finalDist = "0 km";
        else if (!finalDist.contains(" km")) finalDist += " km";

        String finalTime = timeStr;
        if (finalTime.isEmpty() || finalTime.equals("tự động")) finalTime = "0 giờ";
        else {
            if (!finalTime.contains(" giờ")) finalTime += " giờ";
            finalTime = finalTime.replace(".", ",");
        }
        
        data.put("QuangDuong", finalDist);
        data.put("ThoiGian", finalTime);
        data.put("TrangThai", editingRoute != null ? editingRoute.getStatus() : "Đang hoạt động");
        data.put("nhaXe", opUid);

        if (editingRoute == null) {
            String newId = generateNextRouteId();
            data.put("tuyenXeID", newId);
            apiService.createRoute(data).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        showRouteSuccessPopup("Thêm thông tin Tuyến xe thành công");
                        hideRouteForm();
                        fetchRoutesFromApi();
                    }
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(QLTuyenxeActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String id = editingRoute.getId();
            data.put("tuyenXeID", id);
            apiService.updateRoute(id, data).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        showRouteSuccessPopup("Cập nhật thông tin Tuyến xe thành công");
                        hideRouteForm();
                        fetchRoutesFromApi();
                    }
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(QLTuyenxeActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String generateNextRouteId() {
        int maxNum = 0;
        if (routeList != null) {
            for (Route r : routeList) {
                String id = r.getId();
                if (id != null && id.startsWith("TX")) {
                    try {
                        int num = Integer.parseInt(id.substring(2));
                        if (num > maxNum) maxNum = num;
                    } catch (Exception ignored) {}
                }
            }
        }
        return String.format("TX%05d", maxNum + 1);
    }

    private void showRouteSuccessPopup(String msg) {
        View dv = getLayoutInflater().inflate(R.layout.dialog_route_success, null);
        TextView tvMsg = dv.findViewById(R.id.tvRouteSuccessMessage);
        if (tvMsg != null) tvMsg.setText(msg);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(dialog::dismiss, 1500);
    }

    private void showFieldError(EditText edt, TextView tvError, String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        edt.setBackgroundResource(R.drawable.bg_input_error);
    }

    private void clearErrors() {
        tvErrorRouteName.setVisibility(View.GONE);
        tvErrorStartPoint.setVisibility(View.GONE);
        tvErrorEndPoint.setVisibility(View.GONE);
        edtRouteName.setBackgroundResource(R.drawable.bg_input_white);
        edtStartPoint.setBackgroundResource(R.drawable.bg_input_white);
        edtEndPoint.setBackgroundResource(R.drawable.bg_input_white);
    }

    @Override
    public void onEdit(Route route) {
        showRouteForm(route);
    }

    @Override
    public void onDelete(Route route) {
        View dv = getLayoutInflater().inflate(R.layout.dialog_delete_route, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dv.findViewById(R.id.btnNoRoute).setOnClickListener(v -> dialog.dismiss());
        dv.findViewById(R.id.btnYesRoute).setOnClickListener(v -> {
            dialog.dismiss();
            if (route.getStatus().equals("Đang hoạt động") || route.getStatus().equals("Bảo trì")) {
                showErrorPopup("Không thể xóa tuyến xe,\ncó chuyến đang hoạt động");
            } else {
                apiService.deleteRoute(route.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            showRouteSuccessPopup("Xóa Tuyến xe thành công");
                            routeList.remove(route);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override public void onFailure(Call<Void> call, Throwable t) {}
                });
            }
        });

        dialog.show();
    }

    private void showErrorPopup(String msg) {
        View dv = getLayoutInflater().inflate(R.layout.dialog_delete_error, null);
        TextView tvMsg = dv.findViewById(R.id.tvErrorMessage);
        if (tvMsg != null) tvMsg.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(dialog::dismiss, 2000);
    }

    @Override
    public void onStatusChange(Route route) {
        View dv = getLayoutInflater().inflate(R.layout.dialog_status_selection, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dv.findViewById(R.id.btnStatusActive).setOnClickListener(v -> updateStatus(route, "Đang hoạt động", dialog));
        dv.findViewById(R.id.btnStatusMaintain).setOnClickListener(v -> updateStatus(route, "Bảo trì", dialog));
        dv.findViewById(R.id.btnStatusStop).setOnClickListener(v -> updateStatus(route, "Ngưng hoạt động", dialog));

        dialog.show();
    }

    private void updateStatus(Route route, String status, AlertDialog dialog) {
        dialog.dismiss();
        
        Map<String, String> data = new HashMap<>();
        data.put("tuyenXeID", route.getId());
        data.put("tenTuyen", route.getName());
        data.put("diemDi", route.getStartPoint());
        data.put("DiemTrungGian", route.getMidPoint() != null ? route.getMidPoint() : "");
        data.put("diemDen", route.getEndPoint());
        data.put("QuangDuong", route.getDistance());
        data.put("ThoiGian", route.getTime());
        data.put("TrangThai", status);
        data.put("nhaXe", opUid);

        apiService.updateRoute(route.getId(), data).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    route.setStatus(status);
                    sortAndNotify();
                    showRouteSuccessPopup("Cập nhật trạng thái thành công");
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
