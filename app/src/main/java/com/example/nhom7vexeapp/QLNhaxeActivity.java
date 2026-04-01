package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class QLNhaxeActivity extends AppCompatActivity {

    private LinearLayout layoutViewMode, layoutEditMode;
    private TextView txtToolbarTitle, txtFileName;
    private TextView tvViewBusName, tvViewRepName, tvViewAddress, tvViewPhone, tvViewEmail;
    private EditText edtBusName, edtRepName, edtAddress, edtPhone;
    private Button btnEdit, btnSave, btnCancel, btnChooseFile;
    private ImageView btnBack;
    private ImageView imgLogo, imgViewBanner, imgEditPreview;

    // Biến của Xù để điều hướng Phương tiện
    private LinearLayout navVehicle;

    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_nhaxe);

        initViews();       // Ánh xạ
        setupEvents();      // Sự kiện chỉnh sửa/lưu
        setupNavigation();  // Sự kiện thanh Navbar (Quan trọng)
        loadInitialData();  // Load dữ liệu mẫu
    }

    private void initViews() {
        // Toolbar
        btnBack = findViewById(R.id.btnBack);
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);

        // Layout modes
        layoutViewMode = findViewById(R.id.layoutViewMode);
        layoutEditMode = findViewById(R.id.layoutEditMode);

        // View Mode components
        tvViewBusName = findViewById(R.id.tvViewBusName);
        tvViewRepName = findViewById(R.id.tvViewRepName);
        tvViewAddress = findViewById(R.id.tvViewAddress);
        tvViewPhone = findViewById(R.id.tvViewPhone);
        tvViewEmail = findViewById(R.id.tvViewEmail);
        btnEdit = findViewById(R.id.btnEdit);
        imgViewBanner = findViewById(R.id.imgViewBanner);

        // Edit Mode components
        edtBusName = findViewById(R.id.edtBusName);
        edtRepName = findViewById(R.id.edtRepName);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        txtFileName = findViewById(R.id.txtFileName);
        imgEditPreview = findViewById(R.id.imgEditPreview);

        // Ánh xạ nút Phương tiện
        navVehicle = findViewById(R.id.nav_vehicle_op);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (isEditing) showCancelConfirmationDialog();
            else finish();
        });

        btnEdit.setOnClickListener(v -> enterEditMode());

        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());

        btnSave.setOnClickListener(v -> validateAndSave());

        btnChooseFile.setOnClickListener(v -> {
            txtFileName.setText("banner_nhaxe.png");
            Toast.makeText(this, "Đã chọn ảnh thành công", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNavigation() {
        // 1. Xử lý nút Phương tiện của Xù (Dùng v.getContext() để không bị lỗi luồng)
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), PhuongTienManagementActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
                android.util.Log.d("XU_LOG", "Da bam mo màn hình Phuong Tien!");
            });
        }

        // 2. Xử lý nút Tuyến xe (Code của bạn nhóm)
        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                Intent intent = new Intent(this, QLTuyenxeActivity.class);
                startActivity(intent);
            });
        }

        // 3. Xử lý nút Trang chủ (Quay lại OperatorMain)
        LinearLayout navHome = findViewById(R.id.nav_home_op);
        if (navHome == null) {
            navHome = findViewById(R.id.nav_home_op_main);
        }
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
    }

    private void loadInitialData() {
        String name = "Nhà xe Đà Nẵng-Huế";
        String rep = "Tôn Thất Huy Phong";
        String address = "K36/12 Lưu Quang Thuận, Đà Nẵng";
        String phone = "0905509767";
        String email = "dananghue@nhaxe.vn";

        tvViewBusName.setText(name);
        tvViewRepName.setText(rep);
        tvViewAddress.setText(address);
        tvViewPhone.setText(phone);
        tvViewEmail.setText(email);

        edtBusName.setText(name);
        edtRepName.setText(rep);
        edtAddress.setText(address);
        edtPhone.setText(phone);
    }

    private void enterEditMode() {
        isEditing = true;
        layoutViewMode.setVisibility(View.GONE);
        layoutEditMode.setVisibility(View.VISIBLE);
        txtToolbarTitle.setText("Chỉnh sửa Thông tin nhà xe");
    }

    private void exitEditMode() {
        isEditing = false;
        layoutViewMode.setVisibility(View.VISIBLE);
        layoutEditMode.setVisibility(View.GONE);
        txtToolbarTitle.setText("Thông tin nhà xe");
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy")
                .setMessage("Bạn có thông tin chỉnh sửa chưa lưu, xác nhận hủy?")
                .setPositiveButton("Đồng ý", (dialog, which) -> exitEditMode())
                .setNegativeButton("Không", null)
                .show();
    }

    private void validateAndSave() {
        String busName = edtBusName.getText().toString().trim();
        String repName = edtRepName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(busName)) { showError("Vui lòng nhập Tên nhà xe."); return; }
        if (TextUtils.isEmpty(repName)) { showError("Vui lòng nhập Họ tên người đại diện."); return; }
        if (TextUtils.isEmpty(address)) { showError("Vui lòng nhập Địa chỉ trụ sở."); return; }
        if (TextUtils.isEmpty(phone)) { showError("Vui lòng nhập Số điện thoại."); return; }

        if (isSpecialCharStart(busName)) { showError("Tên nhà xe không được bắt đầu bằng ký tự đặc biệt."); return; }
        if (isSpecialCharStart(repName)) { showError("Họ tên người đại diện không được bắt đầu bằng ký tự đặc biệt."); return; }

        if (address.matches(".*[!@#$%^&*()].*")) {
            showError("Địa chỉ trụ sở chứa ký tự đặc biệt không hợp lệ.");
            return;
        }

        if (!phone.matches("0\\d{9}")) {
            showError("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
            return;
        }

        updateViewMode(busName, repName, address, phone);
        Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
        exitEditMode();
    }

    private void updateViewMode(String name, String rep, String addr, String phone) {
        tvViewBusName.setText(name);
        tvViewRepName.setText(rep);
        tvViewAddress.setText(addr);
        tvViewPhone.setText(phone);
    }

    private boolean isSpecialCharStart(String text) {
        if (TextUtils.isEmpty(text)) return false;
        char firstChar = text.charAt(0);
        return !Character.isLetterOrDigit(firstChar);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}