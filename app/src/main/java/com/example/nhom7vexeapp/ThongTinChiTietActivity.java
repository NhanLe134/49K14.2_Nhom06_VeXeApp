package com.example.nhom7vexeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ThongTinChiTietActivity extends AppCompatActivity {

    private EditText edtPickUp, edtDropOff, edtCustomerName, edtPhone;
    private Button btnContinue;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_chi_tiet);

        initViews();

        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            if (validateInfo()) {
                showSuccessDialog();
            }
        });
    }

    private void initViews() {
        edtPickUp = findViewById(R.id.edtPickUp);
        edtDropOff = findViewById(R.id.edtDropOff);
        edtCustomerName = findViewById(R.id.edtCustomerName);
        edtPhone = findViewById(R.id.edtPhone);
        btnContinue = findViewById(R.id.btnContinue);
        btnBack = findViewById(R.id.btnBack);
    }

    private boolean validateInfo() {
        if (edtPickUp.getText().toString().trim().isEmpty() ||
            edtDropOff.getText().toString().trim().isEmpty() ||
            edtCustomerName.getText().toString().trim().isEmpty() ||
            edtPhone.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo");
        builder.setMessage("Đặt vé thành công!");
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Chuyển về màn hình Quản lý vé (mặc định tab Đã đặt)
            Intent intent = new Intent(ThongTinChiTietActivity.this, QLVeXeActivity.class);
            // Thêm flag để báo có vé mới (Giả lập)
            intent.putExtra("new_ticket", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }
}
