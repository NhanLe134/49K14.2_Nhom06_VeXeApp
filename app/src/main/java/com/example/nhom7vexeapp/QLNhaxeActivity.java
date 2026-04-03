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

public class QLNhaxeActivity extends AppCompatActivity {

    private LinearLayout layoutViewMode, layoutEditMode;
    private TextView txtToolbarTitle, txtFileName;
    private TextView tvViewBusName, tvViewRepName, tvViewAddress, tvViewPhone, tvViewEmail;
    private EditText edtBusName, edtRepName, edtAddress, edtPhone;
    private TextView tvErrorBusName, tvErrorRepName, tvErrorAddress, tvErrorPhone;
    private Button btnEdit, btnSave, btnCancel, btnChooseFile;
    private ImageView btnBack;
    private ImageView imgLogo, imgViewBanner, imgEditPreview;

    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_nhaxe);

        initViews();
        setupEvents();
        setupBottomNavigation();
        loadInitialData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
        layoutViewMode = findViewById(R.id.layoutViewMode);
        layoutEditMode = findViewById(R.id.layoutEditMode);
        tvViewBusName = findViewById(R.id.tvViewBusName);
        tvViewRepName = findViewById(R.id.tvViewRepName);
        tvViewAddress = findViewById(R.id.tvViewAddress);
        tvViewPhone = findViewById(R.id.tvViewPhone);
        tvViewEmail = findViewById(R.id.tvViewEmail);
        btnEdit = findViewById(R.id.btnEdit);
        imgViewBanner = findViewById(R.id.imgViewBanner);
        
        edtBusName = findViewById(R.id.edtBusName);
        edtRepName = findViewById(R.id.edtRepName);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);

        tvErrorBusName = findViewById(R.id.tvErrorBusName);
        tvErrorRepName = findViewById(R.id.tvErrorRepName);
        tvErrorAddress = findViewById(R.id.tvErrorAddress);
        tvErrorPhone = findViewById(R.id.tvErrorPhone);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        txtFileName = findViewById(R.id.txtFileName);
        imgEditPreview = findViewById(R.id.imgEditPreview);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (isEditing) {
                showCancelConfirmationDialog();
            } else {
                finish();
            }
        });

        btnEdit.setOnClickListener(v -> enterEditMode());
        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());
        btnSave.setOnClickListener(v -> validateAndSave());
        btnChooseFile.setOnClickListener(v -> {
            txtFileName.setText("banner_nhaxe.png");
            Toast.makeText(this, "Đã chọn ảnh thành công", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupBottomNavigation() {
        // Tab Trang chủ
        View navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        // Tab Tài xế (Hiện tại)
        View navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> {
                // Đang ở trang này
            });
        }

        // Tab Phương tiện
        View navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                startActivity(new Intent(this, PhuongTienManagementActivity.class));
            });
        }

        // Tab Chuyến xe
        View navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                startActivity(new Intent(this, TripListActivity.class));
            });
        }

        // Tab Tuyến xe
        View navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                startActivity(new Intent(this, QLTuyenxeActivity.class));
            });
        }
    }

    private void loadInitialData() {
        String name = "Nhà xe Đà Nẵng-Huế";
        String rep = "Tôn Thất Huy Phong";
        String address = "K36/1 Lưu Quang Thuận, Đà Nẵng";
        String phone = "0905509767";
        String email = "dananghue@nhaxe.vn";

        if (tvViewBusName != null) tvViewBusName.setText(name);
        if (tvViewRepName != null) tvViewRepName.setText(rep);
        if (tvViewAddress != null) tvViewAddress.setText(address);
        if (tvViewPhone != null) tvViewPhone.setText(phone);
        if (tvViewEmail != null) tvViewEmail.setText(email);

        if (edtBusName != null) edtBusName.setText(name);
        if (edtRepName != null) edtRepName.setText(rep);
        if (edtAddress != null) edtAddress.setText(address);
        if (edtPhone != null) edtPhone.setText(phone);
    }

    private void enterEditMode() {
        isEditing = true;
        if (layoutViewMode != null) layoutViewMode.setVisibility(View.GONE);
        if (layoutEditMode != null) layoutEditMode.setVisibility(View.VISIBLE);
        if (txtToolbarTitle != null) txtToolbarTitle.setText("Chỉnh sửa Thông tin nhà xe");
        clearErrors();
    }

    private void exitEditMode() {
        isEditing = false;
        if (layoutViewMode != null) layoutViewMode.setVisibility(View.VISIBLE);
        if (layoutEditMode != null) layoutEditMode.setVisibility(View.GONE);
        if (txtToolbarTitle != null) txtToolbarTitle.setText("Thông tin nhà xe");
    }

    private void showCancelConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_cancel, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnNo = dialogView.findViewById(R.id.btnNo);
        Button btnYes = dialogView.findViewById(R.id.btnYes);

        if (btnNo != null) btnNo.setOnClickListener(v -> dialog.dismiss());
        if (btnYes != null) btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            exitEditMode();
        });

        dialog.show();
    }

    private void validateAndSave() {
        clearErrors();
        boolean isValid = true;

        String busName = edtBusName.getText().toString().trim();
        String repName = edtRepName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(busName)) {
            showFieldError(edtBusName, tvErrorBusName, "Vui lòng nhập Tên nhà xe.");
            isValid = false;
        }
        if (TextUtils.isEmpty(repName)) {
            showFieldError(edtRepName, tvErrorRepName, "Vui lòng nhập Họ tên người đại diện.");
            isValid = false;
        }
        if (TextUtils.isEmpty(address)) {
            showFieldError(edtAddress, tvErrorAddress, "Vui lòng nhập Địa chỉ trụ sở.");
            isValid = false;
        }
        if (TextUtils.isEmpty(phone)) {
            showFieldError(edtPhone, tvErrorPhone, "Vui lòng nhập Số điện thoại.");
            isValid = false;
        }

        if (!isValid) return;

        if (isValid) {
            updateViewMode(busName, repName, address, phone);
            showSuccessPopup();
        }
    }

    private void showFieldError(EditText editText, TextView errorTextView, String message) {
        editText.setBackgroundResource(R.drawable.bg_input_error);
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    private void clearErrors() {
        if (edtBusName != null) edtBusName.setBackgroundResource(R.drawable.bg_input_white);
        if (edtRepName != null) edtRepName.setBackgroundResource(R.drawable.bg_input_white);
        if (edtAddress != null) edtAddress.setBackgroundResource(R.drawable.bg_input_white);
        if (edtPhone != null) edtPhone.setBackgroundResource(R.drawable.bg_input_white);

        if (tvErrorBusName != null) tvErrorBusName.setVisibility(View.GONE);
        if (tvErrorRepName != null) tvErrorRepName.setVisibility(View.GONE);
        if (tvErrorAddress != null) tvErrorAddress.setVisibility(View.GONE);
        if (tvErrorPhone != null) tvErrorPhone.setVisibility(View.GONE);
    }

    private void showSuccessPopup() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_success, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                exitEditMode();
            }
        }, 2000);
    }

    private void updateViewMode(String name, String rep, String addr, String phone) {
        if (tvViewBusName != null) tvViewBusName.setText(name);
        if (tvViewRepName != null) tvViewRepName.setText(rep);
        if (tvViewAddress != null) tvViewAddress.setText(addr);
        if (tvViewPhone != null) tvViewPhone.setText(phone);
    }
}
