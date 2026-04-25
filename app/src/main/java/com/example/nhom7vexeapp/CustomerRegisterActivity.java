package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nhom7vexeapp.utils.IdGenerator;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;

public class CustomerRegisterActivity extends AppCompatActivity {

    private LinearLayout layoutStepPhone;
    private ScrollView layoutStepForm;
    private EditText edtPhoneInput, edtFullName, edtDob;
    private TextView tvFixedPhone, tvFileName, tvHeaderTitle;
    private TextView tvErrorFullName, tvErrorDob, tvErrorFile;
    private MaterialButton btnVerifyPhone, btnFinish, btnCancel, btnSelectFile;
    private String verifiedPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        initViews();
        setupEvents();
    }

    private void initViews() {
        layoutStepPhone = findViewById(R.id.layoutStepPhone);
        layoutStepForm = findViewById(R.id.layoutStepForm);
        edtPhoneInput = findViewById(R.id.edtPhoneInput);
        btnVerifyPhone = findViewById(R.id.btnVerifyPhone);
        edtFullName = findViewById(R.id.edtFullName);
        edtDob = findViewById(R.id.edtDob);
        tvFixedPhone = findViewById(R.id.tvFixedPhone);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvErrorFullName = findViewById(R.id.tvErrorFullName);
        tvErrorDob = findViewById(R.id.tvErrorDob);
        btnFinish = findViewById(R.id.btnFinish);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        btnVerifyPhone.setOnClickListener(v -> {
            String phone = edtPhoneInput.getText().toString().trim();
            if (phone.length() < 10) {
                Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            } else {
                verifiedPhone = phone;
                showOtpDialog();
            }
        });

        edtDob.setOnClickListener(v -> showDatePicker());

        btnFinish.setOnClickListener(v -> {
            if (validateForm()) {
                saveUserData();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("registeredPhone", verifiedPhone);
                startActivity(intent);
                finish();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveUserData() {
        // Sinh ID tự động KHxxxxx
        String newId = IdGenerator.generateKhachHangID(this);
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        
        editor.putString("id_" + verifiedPhone, newId);
        editor.putString("name_" + verifiedPhone, edtFullName.getText().toString().trim());
        editor.putString("dob_" + verifiedPhone, edtDob.getText().toString().trim());
        
        editor.putString("khachHangID", newId);
        editor.putString("customerName", edtFullName.getText().toString().trim());
        editor.apply();

        Toast.makeText(this, "Đăng ký thành công! ID: " + newId, Toast.LENGTH_LONG).show();
    }

    private void showOtpDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_otp, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        dialog.show();

        Button btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp);
        btnVerifyOtp.setOnClickListener(v -> {
            dialog.dismiss();
            moveToForm();
        });
    }

    private void moveToForm() {
        layoutStepPhone.setVisibility(View.GONE);
        layoutStepForm.setVisibility(View.VISIBLE);
        tvFixedPhone.setText(verifiedPhone);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            edtDob.setText(String.format("%02d/%02d/%d", d, m + 1, y));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean validateForm() {
        if (edtFullName.getText().toString().isEmpty()) return false;
        if (edtDob.getText().toString().isEmpty()) return false;
        return true;
    }
}
