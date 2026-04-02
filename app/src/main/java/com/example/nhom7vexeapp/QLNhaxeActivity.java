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
<<<<<<< Updated upstream
        setupBottomNavigation();
        // Giả lập load dữ liệu ban đầu
=======
        setupNavigation();
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    private void setupBottomNavigation() {
        // Home
        View navHome = findViewById(R.id.nav_home_op);
=======
    private void setupNavigation() {
        LinearLayout navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                Intent intent = new Intent(this, QLTuyenxeActivity.class);
                startActivity(intent);
            });
        }
        
        LinearLayout navHome = findViewById(R.id.nav_home_op);
>>>>>>> Stashed changes
        if (navHome == null) navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        // Vehicle
        View navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                Intent intent = new Intent(this, PhuongTienManagementActivity.class);
                startActivity(intent);
            });
        }

        // Trip
        View navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                Intent intent = new Intent(this, TripListActivity.class);
                startActivity(intent);
            });
        }

        // Route
        View navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                Intent intent = new Intent(this, QLTuyenxeActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadInitialData() {
        String name = "Nhà xe Đà Nẵng-Huế";
        String rep = "Tôn Thất Huy Phong";
        String address = "K36/1 Lưu Quang Thuận, Đà Nẵng";
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
        clearErrors();
    }

    private void exitEditMode() {
        isEditing = false;
        layoutViewMode.setVisibility(View.VISIBLE);
        layoutEditMode.setVisibility(View.GONE);
        txtToolbarTitle.setText("Thông tin nhà xe");
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

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
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

        // Kiểm tra trống
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

        // Kiểm tra hợp lệ
        if (isSpecialCharStart(busName)) {
            showFieldError(edtBusName, tvErrorBusName, "Tên nhà xe không bắt đầu bằng ký tự đặc biệt.");
            isValid = false;
        }
        if (isSpecialCharStart(repName)) {
            showFieldError(edtRepName, tvErrorRepName, "Họ tên người đại diện không bắt đầu bằng ký tự đặc biệt.");
            isValid = false;
        }
        if (address.matches(".*[!@#$%^&*()].*")) {
            showFieldError(edtAddress, tvErrorAddress, "Địa chỉ trụ sở chứa ký tự đặc biệt không hợp lệ.");
            isValid = false;
        }
        if (!phone.matches("0\\d{9}")) {
            showFieldError(edtPhone, tvErrorPhone, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
            isValid = false;
        }

        if (isValid) {
            updateViewMode(busName, repName, address, phone);
            showSuccessPopup();
        }
    }

    private void showFieldError(EditText editText, TextView errorTextView, String message) {
        editText.setBackgroundResource(R.drawable.bg_input_error);
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void clearErrors() {
        edtBusName.setBackgroundResource(R.drawable.bg_input_white);
        edtRepName.setBackgroundResource(R.drawable.bg_input_white);
        edtAddress.setBackgroundResource(R.drawable.bg_input_white);
        edtPhone.setBackgroundResource(R.drawable.bg_input_white);

        tvErrorBusName.setVisibility(View.GONE);
        tvErrorRepName.setVisibility(View.GONE);
        tvErrorAddress.setVisibility(View.GONE);
        tvErrorPhone.setVisibility(View.GONE);
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

        dialogView.setOnClickListener(v -> {
            dialog.dismiss();
            exitEditMode();
        });
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
