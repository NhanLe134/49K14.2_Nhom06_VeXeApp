package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        opUid = pref.getString("op_uid", "NX001");
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
                        // Lọc theo mã nhà xe nếu cần
                        routeList.add(r);
                    }
                    sortAndNotify();
                }
            }
            @Override public void onFailure(Call<List<Route>> call, Throwable t) {
                Toast.makeText(QLTuyenxeActivity.this, "Không thể tải danh sách tuyến xe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortAndNotify() {
        Collections.sort(routeList, (r1, r2) -> getStatusPriority(r1.getStatus()) - getStatusPriority(r2.getStatus()));
        adapter.notifyDataSetChanged();
    }

    private int getStatusPriority(String status) {
        if (status == null) return 0;
        if (status.equalsIgnoreCase("Đang hoạt động")) return 0;
        if (status.equalsIgnoreCase("Bảo trì")) return 1;
        if (status.equalsIgnoreCase("Ngưng hoạt động")) return 2;
        return 3;
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (inlineFormCard.getVisibility() == View.VISIBLE) {
                showCancelConfirmationDialog(this::hideRouteForm);
            } else backToHome();
        });

        btnAddRoute.setOnClickListener(v -> showRouteForm(null));
        btnCancelForm.setOnClickListener(v -> showCancelConfirmationDialog(this::hideRouteForm));
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
        String start = edtStartPoint.getText().toString().trim();
        String end = edtEndPoint.getText().toString().trim();

        if (start.isEmpty() || end.isEmpty()) {
            edtAutoDistance.setText("tự động");
            edtAutoDistance.setTextColor(Color.GRAY);
            edtAutoTime.setText("tự động");
            edtAutoTime.setTextColor(Color.GRAY);
            return;
        }

        // Logic giả lập tính toán khoảng cách/thời gian
        int totalDist = (start.length() + end.length()) * 5 + 10;
        float totalTime = (float) totalDist / 45;

        edtAutoDistance.setText(totalDist + " km");
        edtAutoDistance.setTextColor(Color.BLACK);
        edtAutoTime.setText(String.format(Locale.getDefault(), "%.1f giờ", totalTime));
        edtAutoTime.setTextColor(Color.BLACK);
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
        View dv = getLayoutInflater().inflate(R.layout.dialog_cancel_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dv.findViewById(R.id.btnDialogNo).setOnClickListener(v -> dialog.dismiss());
        dv.findViewById(R.id.btnDialogYes).setOnClickListener(v -> {
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
            edtAutoTime.setText(route.getTime());
            edtAutoDistance.setTextColor(Color.BLACK);
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
        String name = edtRouteName.getText().toString().trim();
        String start = edtStartPoint.getText().toString().trim();
        String end = edtEndPoint.getText().toString().trim();

        if (name.isEmpty()) { showFieldError(edtRouteName, tvErrorRouteName, "Nhập tên tuyến."); return; }
        if (start.isEmpty()) { showFieldError(edtStartPoint, tvErrorStartPoint, "Nhập điểm đi."); return; }
        if (end.isEmpty()) { showFieldError(edtEndPoint, tvErrorEndPoint, "Nhập điểm đến."); return; }

        Map<String, String> data = new HashMap<>();
        data.put("tenTuyen", name);
        data.put("diemDi", start);
        data.put("DiemTrungGian", edtMidPoint.getText().toString().trim());
        data.put("diemDen", end);
        data.put("QuangDuong", edtAutoDistance.getText().toString());
        data.put("ThoiGian", edtAutoTime.getText().toString());
        data.put("nhaXe", opUid);

        if (editingRoute == null) {
            apiService.createRoute(data).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> res) {
                    if (res.isSuccessful()) {
                        showRouteSuccessPopup("Thêm tuyến xe thành công");
                        hideRouteForm();
                        fetchRoutesFromApi();
                    }
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {}
            });
        } else {
            apiService.updateRoute(editingRoute.getId(), data).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> res) {
                    if (res.isSuccessful()) {
                        showRouteSuccessPopup("Cập nhật tuyến xe thành công");
                        hideRouteForm();
                        fetchRoutesFromApi();
                    }
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
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

    @Override public void onEdit(Route route) { showRouteForm(route); }

    @Override public void onDelete(Route route) {
        View dv = getLayoutInflater().inflate(R.layout.dialog_delete_route, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dv.findViewById(R.id.btnNoRoute).setOnClickListener(v -> dialog.dismiss());
        dv.findViewById(R.id.btnYesRoute).setOnClickListener(v -> {
            dialog.dismiss();
            apiService.deleteRoute(route.getId()).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        showRouteSuccessPopup("Xóa Tuyến xe thành công");
                        fetchRoutesFromApi();
                    }
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {}
            });
        });
        dialog.show();
    }

    private void showErrorPopup(String msg) {
        View dv = getLayoutInflater().inflate(R.layout.dialog_delete_error, null);
        TextView tvMsg = dv.findViewById(R.id.tvDialogMessage);
        if (tvMsg != null) tvMsg.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(dialog::dismiss, 2000);
    }

    @Override public void onStatusChange(Route route) {}
}
