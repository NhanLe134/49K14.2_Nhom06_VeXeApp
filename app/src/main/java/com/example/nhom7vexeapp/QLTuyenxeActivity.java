package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.adapters.RouteAdapter;
import com.example.nhom7vexeapp.models.Route;
import com.google.android.material.button.MaterialButton;

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

    // Inline Form views
    private CardView inlineFormCard;
    private TextView tvFormGuide;
    private EditText edtRouteName, edtStartPoint, edtMidPoint, edtEndPoint;
    private MaterialButton btnSaveForm, btnCancelForm;

    private Route editingRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_tuyenxe);

        initViews();
        setupRecyclerView();
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
        btnSaveForm = findViewById(R.id.btnSaveForm);
        btnCancelForm = findViewById(R.id.btnCancelForm);
    }

    private void setupRecyclerView() {
        routeList = new ArrayList<>();
        routeList.add(new Route("R001", "Tuyến Đà Nẵng – Huế", "Đà Nẵng", "", "Huế", "100 km", "≈ 2.5 giờ", "Đang hoạt động"));
        routeList.add(new Route("R002", "Tuyến Huế – Đà Nẵng", "Huế", "", "Đà Nẵng", "100 km", "≈ 2.5 giờ", "Đang hoạt động"));
        routeList.add(new Route("R003", "Tuyến Đà Nẵng – Hội An", "Đà Nẵng", "", "Hội An", "30 km", "≈ 30 phút", "Đang hoạt động"));
        routeList.add(new Route("R004", "Tuyến Hội An – Đà Nẵng", "Hội An", "", "Đà Nẵng", "30 km", "≈ 1 giờ", "Đang hoạt động"));
        routeList.add(new Route("R005", "Tuyến Hội An – Quảng Nam", "Hội An", "", "Quảng Nam", "70 km", "≈ 1,5 giờ", "Ngưng hoạt động"));

        adapter = new RouteAdapter(routeList, this);
        rvRoutes.setLayoutManager(new LinearLayoutManager(this));
        rvRoutes.setAdapter(adapter);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (inlineFormCard.getVisibility() == View.VISIBLE) {
                hideRouteForm();
            } else {
                finish();
            }
        });

        btnAddRoute.setOnClickListener(v -> showRouteForm(null));

        btnCancelForm.setOnClickListener(v -> {
            String message = (editingRoute == null) ? "Bạn có thông tin thêm mới chưa lưu, xác nhận hủy?" : "Bạn có thông tin chỉnh sửa chưa lưu, xác nhận hủy?";
            new AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage(message)
                    .setPositiveButton("Đồng ý", (dialog, which) -> hideRouteForm())
                    .setNegativeButton("Không", null)
                    .show();
        });

        btnSaveForm.setOnClickListener(v -> validateAndSave());
    }

    private void setupNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home_op_main);
        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(this, OperatorMainActivity.class));
                finish();
            });
        }
        
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                startActivity(new Intent(this, TripListActivity.class));
                finish();
            });
        }
    }

    private void showRouteForm(Route route) {
        editingRoute = route;
        rvRoutes.setVisibility(View.GONE);
        btnAddRoute.setVisibility(View.GONE);
        inlineFormCard.setVisibility(View.VISIBLE);

        if (route == null) {
            tvToolbarTitle.setText("Thêm Tuyến xe");
            tvFormGuide.setText("Vui lòng nhập thông tin tuyến xe. Các trường có dấu (*) là bắt buộc.");
            clearForm();
        } else {
            tvToolbarTitle.setText("Sửa thông tin Tuyến xe");
            tvFormGuide.setText("Bạn có thể chỉnh sửa thông tin tuyến xe bên dưới. Các trường có dấu (*) là bắt buộc.");
            edtRouteName.setText(route.getName());
            edtStartPoint.setText(route.getStartPoint());
            edtMidPoint.setText(route.getMidPoint());
            edtEndPoint.setText(route.getEndPoint());
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
        edtRouteName.setText("");
        edtStartPoint.setText("");
        edtMidPoint.setText("");
        edtEndPoint.setText("");
    }

    private void validateAndSave() {
        String name = edtRouteName.getText().toString().trim();
        String start = edtStartPoint.getText().toString().trim();
        String end = edtEndPoint.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingRoute == null) {
            Route newRoute = new Route(UUID.randomUUID().toString(), name, start, edtMidPoint.getText().toString(), end, "100 km", "≈ 2.5 giờ", "Đang hoạt động");
            routeList.add(newRoute);
            Toast.makeText(this, "Thêm tuyến xe thành công", Toast.LENGTH_SHORT).show();
        } else {
            editingRoute.setName(name);
            editingRoute.setStartPoint(start);
            editingRoute.setMidPoint(edtMidPoint.getText().toString());
            editingRoute.setEndPoint(end);
            Toast.makeText(this, "Cập nhật tuyến xe thành công", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
        hideRouteForm();
    }

    @Override
    public void onEdit(Route route) { showRouteForm(route); }

    @Override
    public void onDelete(Route route) {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Bạn có chắc muốn xóa Tuyến xe này không?\nHành động này không thể hoàn tác.")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    if ("Đang hoạt động".equals(route.getStatus())) {
                        new AlertDialog.Builder(this)
                                .setTitle("Lỗi")
                                .setMessage("Không thể xóa tuyến xe, có chuyến đang hoạt động")
                                .setIcon(android.R.drawable.ic_delete)
                                .setPositiveButton("Đóng", null).show();
                    } else {
                        routeList.remove(route);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Không", null).show();
    }
}
