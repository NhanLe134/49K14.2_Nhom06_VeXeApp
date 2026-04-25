package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.Comparator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.adapters.RouteAdapter;
import com.example.nhom7vexeapp.models.Route;
import com.google.android.material.button.MaterialButton;

import android.content.SharedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        opUid = pref.getString("op_uid", "");
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
        apiService.getRoutes().enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    routeList.clear();
                    for (Route r : response.body()) {
                        if (opUid.equals(r.getNhaXeId())) routeList.add(r);
                    }
                    
                    Collections.sort(routeList, (r1, r2) -> getStatusPriority(r1.getStatus()) - getStatusPriority(r2.getStatus()));
                    adapter.notifyDataSetChanged();
                }
            }
            private int getStatusPriority(String status) {
                if (status == null) return 0;
                if (status.equals("Đang hoạt động")) return 0;
                if (status.equals("Bảo trì")) return 1;
                if (status.equals("Ngưng hoạt động")) return 2;
                return 3;
            }
            @Override public void onFailure(Call<List<Route>> call, Throwable t) {}
        });
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (inlineFormCard.getVisibility() == View.VISIBLE) {
                String msg = (editingRoute == null) ? "Bạn có thông tin thêm mới chưa lưu, xác nhận hủy?" : "Bạn có thông tin chỉnh sửa chưa lưu, xác nhận hủy?";
                showRouteConfirmDialog(msg, this::hideRouteForm);
            } else backToHome();
        });

        btnAddRoute.setOnClickListener(v -> showRouteForm(null));
        btnCancelForm.setOnClickListener(v -> {
            String msg = (editingRoute == null) ? "Bạn có thông tin thêm mới chưa lưu, xác nhận hủy?" : "Bạn có thông tin chỉnh sửa chưa lưu, xác nhận hủy?";
            showRouteConfirmDialog(msg, this::hideRouteForm);
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
        edtAutoDistance.setTextColor(Color.parseColor("#333333"));
        String timeStr = String.format("%.1f giờ", totalTime).replace(".0", "");
        edtAutoTime.setText(timeStr);
        edtAutoTime.setTextColor(Color.parseColor("#333333"));
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

    private void showRouteConfirmDialog(String message, Runnable onConfirm) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_route, null);
        TextView tvMsg = dialogView.findViewById(R.id.tvDialogMessageRoute);
        if (tvMsg != null) tvMsg.setText(message);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogView.findViewById(R.id.btnNoRoute).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnYesRoute).setOnClickListener(v -> { dialog.dismiss(); onConfirm.run(); });
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
            edtAutoDistance.setText(route.getDistance());
            edtAutoDistance.setTextColor(Color.parseColor("#333333"));
            edtAutoTime.setText(route.getTime());
            edtAutoTime.setTextColor(Color.parseColor("#333333"));
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

        if (name.isEmpty()) { showFieldError(edtRouteName, tvErrorRouteName, "Vui lòng nhập tên tuyến xe."); isValid = false; }
        else if (isSpecialCharStart(name)) { showFieldError(edtRouteName, tvErrorRouteName, "Tên tuyến xe không bắt đầu bằng ký tự đặc biệt."); isValid = false; }
        if (start.isEmpty()) { showFieldError(edtStartPoint, tvErrorStartPoint, "Vui lòng nhập điểm đi."); isValid = false; }
        else if (isSpecialCharStart(start)) { showFieldError(edtStartPoint, tvErrorStartPoint, "Điểm đi không bắt đầu bằng ký tự đặc biệt."); isValid = false; }
        if (end.isEmpty()) { showFieldError(edtEndPoint, tvErrorEndPoint, "Vui lòng nhập điểm đến."); isValid = false; }
        else if (isSpecialCharStart(end)) { showFieldError(edtEndPoint, tvErrorEndPoint, "Điểm đến không bắt đầu bằng ký tự đặc biệt."); isValid = false; }

        if (!isValid) return;

        Map<String, String> data = new HashMap<>();
        data.put("tenTuyen", name); data.put("diemDi", start); data.put("DiemTrungGian", edtMidPoint.getText().toString());
        data.put("diemDen", end); data.put("QuangDuong", edtAutoDistance.getText().toString());
        data.put("ThoiGian", edtAutoTime.getText().toString());
        data.put("TrangThai", editingRoute != null ? editingRoute.getStatus() : "Đang hoạt động");
        data.put("nhaXe", opUid);

        String id = (editingRoute == null) ? UUID.randomUUID().toString().substring(0, 10) : editingRoute.getId();
        data.put("tuyenXeID", id);

        apiService.updateRoute(id, data).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    fetchRoutesFromApi();
                    showActionSuccessPopup(editingRoute == null ? "Thêm thông tin Tuyến xe thành công" : "Cập nhật thông tin Tuyến xe thành công");
                    hideRouteForm();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QLTuyenxeActivity.this, "Lỗi kết nối API!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFieldError(EditText editText, TextView errorTextView, String message) {
        editText.setBackgroundResource(R.drawable.bg_input_error);
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void clearErrors() {
        edtRouteName.setBackgroundResource(R.drawable.bg_input_white);
        edtStartPoint.setBackgroundResource(R.drawable.bg_input_white);
        edtEndPoint.setBackgroundResource(R.drawable.bg_input_white);
        tvErrorRouteName.setVisibility(View.GONE);
        tvErrorStartPoint.setVisibility(View.GONE);
        tvErrorEndPoint.setVisibility(View.GONE);
    }

    @Override public void onEdit(Route route) { showRouteForm(route); }
    @Override public void onDelete(Route route) {
        showRouteConfirmDialog("Bạn có chắc muốn xóa Tuyến xe này không?\nHành động này không thể hoàn tác.", () -> {
            if ("Đang hoạt động".equals(route.getStatus())) showActionErrorPopup("Không thể xóa tuyến xe,\ncó chuyến đang hoạt động");
            else { routeList.remove(route); adapter.notifyDataSetChanged(); showActionSuccessPopup("Xóa Tuyến xe thành công"); }
        });
    }

    @Override public void onStatusChange(Route route) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_status_selection, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogView.findViewById(R.id.btnStatusActive).setOnClickListener(v -> { updateRouteStatusApi(route, "Đang hoạt động"); dialog.dismiss(); });
        dialogView.findViewById(R.id.btnStatusMaintain).setOnClickListener(v -> { updateRouteStatusApi(route, "Bảo trì"); dialog.dismiss(); });
        dialogView.findViewById(R.id.btnStatusStop).setOnClickListener(v -> { updateRouteStatusApi(route, "Ngưng hoạt động"); dialog.dismiss(); });
        dialog.show();
    }

    private void updateRouteStatusApi(Route route, String newStatus) {
        Map<String, String> data = new HashMap<>();
        data.put("tuyenXeID", route.getId()); data.put("nhaXe", opUid); data.put("tenTuyen", route.getName());
        data.put("diemDi", route.getStartPoint()); data.put("diemDen", route.getEndPoint());
        data.put("DiemTrungGian", route.getMidPoint()); data.put("QuangDuong", route.getDistance());
        data.put("ThoiGian", route.getTime()); data.put("TrangThai", newStatus);
        apiService.updateRoute(route.getId(), data).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) { fetchRoutesFromApi(); showActionSuccessPopup("Cập nhật trạng thái thành công"); }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void showActionSuccessPopup(String message) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_route_success, null);
        TextView tvMsg = dialogView.findViewById(R.id.tvRouteSuccessMessage);
        if (tvMsg != null) tvMsg.setText(message);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(dialog::dismiss, 2000);
    }

    private void showActionErrorPopup(String message) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_error, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(dialog::dismiss, 2000);
    }

    private boolean isSpecialCharStart(String text) {
        if (text == null || text.isEmpty()) return false;
        return !Character.isLetterOrDigit(text.charAt(0));
    }
}
