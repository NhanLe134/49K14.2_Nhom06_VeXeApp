package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
    private TextView tvErrorRouteName, tvErrorStartPoint, tvErrorEndPoint;
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
        
        tvErrorRouteName = findViewById(R.id.tvErrorRouteName);
        tvErrorStartPoint = findViewById(R.id.tvErrorStartPoint);
        tvErrorEndPoint = findViewById(R.id.tvErrorEndPoint);
        
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
                String msg = (editingRoute == null) ? "Bạn có thông tin thêm mới chưa lưu, xác nhận hủy?" : "Bạn có thông tin chỉnh sửa chưa lưu, xác nhận hủy?";
                showRouteConfirmDialog(msg, this::hideRouteForm);
            } else {
                backToHome();
            }
        });

        btnAddRoute.setOnClickListener(v -> showRouteForm(null));

        btnCancelForm.setOnClickListener(v -> {
            String msg = (editingRoute == null) ? "Bạn có thông tin thêm mới chưa lưu, xác nhận hủy?" : "Bạn có thông tin chỉnh sửa chưa lưu, xác nhận hủy?";
            showRouteConfirmDialog(msg, this::hideRouteForm);
        });

        btnSaveForm.setOnClickListener(v -> validateAndSave());
    }

    private void backToHome() {
        Intent intent = new Intent(this, OperatorMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> backToHome());
        }

        LinearLayout navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                Intent intent = new Intent(this, PhuongTienManagementActivity.class);
                startActivity(intent);
            });
        }

        LinearLayout navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                Intent intent = new Intent(this, TripListActivity.class);
                startActivity(intent);
            });
        }

        LinearLayout navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> {
                Intent intent = new Intent(this, QLNhaxeActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showRouteConfirmDialog(String message, Runnable onConfirm) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_route, null);
        TextView tvMsg = dialogView.findViewById(R.id.tvDialogMessageRoute);
        tvMsg.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnNoRoute).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnYesRoute).setOnClickListener(v -> {
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
        clearErrors();
        boolean isValid = true;

        String name = edtRouteName.getText().toString().trim();
        String start = edtStartPoint.getText().toString().trim();
        String end = edtEndPoint.getText().toString().trim();

        // Kiểm tra Tên tuyến xe
        if (name.isEmpty()) {
            showFieldError(edtRouteName, tvErrorRouteName, "Vui lòng nhập tên tuyến xe.");
            isValid = false;
        } else if (isSpecialCharStart(name)) {
            showFieldError(edtRouteName, tvErrorRouteName, "Tên tuyến xe không bắt đầu bằng ký tự đặc biệt.");
            isValid = false;
        }

        // Kiểm tra Điểm đi
        if (start.isEmpty()) {
            showFieldError(edtStartPoint, tvErrorStartPoint, "Vui lòng nhập điểm đi.");
            isValid = false;
        } else if (isSpecialCharStart(start)) {
            showFieldError(edtStartPoint, tvErrorStartPoint, "Điểm đi không bắt đầu bằng ký tự đặc biệt.");
            isValid = false;
        }

        // Kiểm tra Điểm đến
        if (end.isEmpty()) {
            showFieldError(edtEndPoint, tvErrorEndPoint, "Vui lòng nhập điểm đến.");
            isValid = false;
        } else if (isSpecialCharStart(end)) {
            showFieldError(edtEndPoint, tvErrorEndPoint, "Điểm đến không bắt đầu bằng ký tự đặc biệt.");
            isValid = false;
        }

        if (!isValid) return;

        if (editingRoute == null) {
            Route newRoute = new Route(UUID.randomUUID().toString(), name, start, edtMidPoint.getText().toString(), end, "100 km", "≈ 2.5 giờ", "Đang hoạt động");
            routeList.add(newRoute);
            showActionSuccessPopup("Thêm thông tin Tuyến xe thành công");
        } else {
            editingRoute.setName(name);
            editingRoute.setStartPoint(start);
            editingRoute.setMidPoint(edtMidPoint.getText().toString());
            editingRoute.setEndPoint(end);
            showActionSuccessPopup("Cập nhật thông tin Tuyến xe thành công");
        }

        adapter.notifyDataSetChanged();
        hideRouteForm();
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

    private boolean isSpecialCharStart(String text) {
        if (text == null || text.isEmpty()) return false;
        char firstChar = text.charAt(0);
        return !Character.isLetterOrDigit(firstChar);
    }

    @Override
    public void onEdit(Route route) { showRouteForm(route); }

    @Override
    public void onDelete(Route route) {
        showRouteConfirmDialog("Bạn có chắc muốn xóa Tuyến xe này không?\nHành động này không thể hoàn tác.", () -> {
            if ("Đang hoạt động".equals(route.getStatus())) {
                showActionErrorPopup("Không thể xóa tuyến xe,\ncó chuyến đang hoạt động");
            } else {
                routeList.remove(route);
                adapter.notifyDataSetChanged();
                showActionSuccessPopup("Xóa Tuyến xe thành công");
            }
        });
    }

    private void showActionSuccessPopup(String message) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_route_success, null);
        TextView tvMsg = dialogView.findViewById(R.id.tvRouteSuccessMessage);
        tvMsg.setText(message);
        
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        new Handler().postDelayed(dialog::dismiss, 2000);
    }

    private void showActionErrorPopup(String message) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_error, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        new Handler().postDelayed(dialog::dismiss, 2000);
    }
}
