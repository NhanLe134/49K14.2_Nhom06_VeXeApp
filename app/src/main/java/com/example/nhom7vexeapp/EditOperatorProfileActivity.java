package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class EditOperatorProfileActivity extends AppCompatActivity {

    private EditText edtName, edtRep, edtAddress, edtPhone;
    private TextView tvErrorName, tvErrorRep, tvErrorAddress, tvErrorPhone;
    private MaterialButton btnSave, btnCancel;
    private LinearLayout navHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_operator_profile);

        initViews();
        loadCurrentData();

        findViewById(R.id.btnBack).setOnClickListener(v -> showCancelConfirmation());
        btnCancel.setOnClickListener(v -> showCancelConfirmation());

        // Nút Home ở Bottom Nav
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, OperatorMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                saveUpdatedData();
                showSuccessPopup();
            }
        });
    }

    private void initViews() {
        edtName = findViewById(R.id.edtEditOpName);
        edtRep = findViewById(R.id.edtEditOpRep);
        edtAddress = findViewById(R.id.edtEditOpAddress);
        edtPhone = findViewById(R.id.edtEditOpPhone);
        
        tvErrorName = findViewById(R.id.tvErrorOpName);
        tvErrorRep = findViewById(R.id.tvErrorOpRep);
        tvErrorAddress = findViewById(R.id.tvErrorOpAddress);
        tvErrorPhone = findViewById(R.id.tvErrorOpPhone);
        
        btnSave = findViewById(R.id.btnSaveEditOp);
        btnCancel = findViewById(R.id.btnCancelEditOp);
        navHome = findViewById(R.id.navHomeEditProfile);
    }

    private void loadCurrentData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        edtName.setText(pref.getString("op_name", "Nhà xe Đà Nẵng-Huế"));
        edtRep.setText(pref.getString("op_rep", "Tôn Thất Huy Phong"));
        edtAddress.setText(pref.getString("op_address", "K36/1 Lưu Quang Thuận, Đà Nẵng"));
        edtPhone.setText(pref.getString("op_phone", "0905509767"));
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Reset trạng thái
        clearErrors();

        String name = edtName.getText().toString().trim();
        String rep = edtRep.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        // Kiểm tra trống
        if (name.isEmpty()) {
            showFieldError(edtName, tvErrorName, "Vui lòng nhập tên nhà xe.");
            isValid = false;
        } else if (isSpecialCharStart(name)) {
            showFieldError(edtName, tvErrorName, "Tên nhà xe không bắt đầu bằng ký tự đặc biệt.");
            isValid = false;
        }

        if (rep.isEmpty()) {
            showFieldError(edtRep, tvErrorRep, "Vui lòng nhập họ tên người đại diện.");
            isValid = false;
        } else if (isSpecialCharStart(rep)) {
            showFieldError(edtRep, tvErrorRep, "Họ tên người đại diện không bắt đầu bằng ký tự đặc biệt.");
            isValid = false;
        }

        if (address.isEmpty()) {
            showFieldError(edtAddress, tvErrorAddress, "Vui lòng nhập địa chỉ trụ sở.");
            isValid = false;
        } else if (address.matches(".*[!@#$%^&*()].*")) {
            showFieldError(edtAddress, tvErrorAddress, "Địa chỉ trụ sở chứa ký tự đặc biệt không hợp lệ.");
            isValid = false;
        }

        if (phone.isEmpty()) {
            showFieldError(edtPhone, tvErrorPhone, "Vui lòng nhập số điện thoại.");
            isValid = false;
        } else if (!phone.matches("0\\d{9}")) {
            showFieldError(edtPhone, tvErrorPhone, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.");
            isValid = false;
        }

        return isValid;
    }

    private void showFieldError(EditText editText, TextView errorTextView, String message) {
        editText.setBackgroundResource(R.drawable.bg_input_error);
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void clearErrors() {
        edtName.setBackgroundResource(R.drawable.bg_input_white);
        edtRep.setBackgroundResource(R.drawable.bg_input_white);
        edtAddress.setBackgroundResource(R.drawable.bg_input_white);
        edtPhone.setBackgroundResource(R.drawable.bg_input_white);

        tvErrorName.setVisibility(View.GONE);
        tvErrorRep.setVisibility(View.GONE);
        tvErrorAddress.setVisibility(View.GONE);
        tvErrorPhone.setVisibility(View.GONE);
    }

    private boolean isSpecialCharStart(String text) {
        if (text == null || text.isEmpty()) return false;
        char firstChar = text.charAt(0);
        return !Character.isLetterOrDigit(firstChar);
    }

    private void saveUpdatedData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("op_name", edtName.getText().toString().trim());
        editor.putString("op_rep", edtRep.getText().toString().trim());
        editor.putString("op_address", edtAddress.getText().toString().trim());
        editor.putString("op_phone", edtPhone.getText().toString().trim());
        editor.apply();
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
                setResult(RESULT_OK);
                finish();
            }
        }, 2000);

        dialogView.setOnClickListener(v -> {
            dialog.dismiss();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void showCancelConfirmation() {
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
            finish();
        });

        dialog.show();
    }
}
