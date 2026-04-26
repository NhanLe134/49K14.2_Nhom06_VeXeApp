package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.api.CustomerResponse;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCustomerProfileActivity extends AppCompatActivity {

    private EditText edtName, edtDob;
    private TextView tvPhone;
    private MaterialButton btnSave, btnCancel;
    private String customerUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_customer_profile);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        customerUid = pref.getString("customerUid", "");
        if (customerUid.isEmpty()) customerUid = pref.getString("user_id", "");

        initViews();
        
        // Ưu tiên hiển thị SĐT đã lưu trong máy trước
        String savedPhone = pref.getString("customerPhone", "");
        if (!savedPhone.isEmpty()) tvPhone.setText(savedPhone);
        
        loadData();

        edtDob.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> updateProfile());
        btnCancel.setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        edtName = findViewById(R.id.edtEditFullName);
        edtDob = findViewById(R.id.edtEditDob);
        tvPhone = findViewById(R.id.tvEditPhone);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnCancel = findViewById(R.id.btnCancelEdit);
    }

    private void loadData() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        
        // 1. Lấy thông tin chi tiết khách hàng (Tên, Ngày sinh)
        apiService.getKhachHangDetail(customerUid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    edtName.setText(find(data, "Hovaten", "TenKhachHang", "name"));
                    String dob = find(data, "Ngaysinh", "NgaySinh");
                    if (dob.contains("T")) dob = dob.split("T")[0];
                    edtDob.setText(dob);
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });

        // 2. Lấy SĐT từ Auth API nếu chưa có
        apiService.getUserAuthDetail(customerUid).enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String sdt = response.body().getSdt();
                    if (sdt != null && !sdt.isEmpty()) tvPhone.setText(sdt);
                }
            }
            @Override public void onFailure(Call<CustomerResponse> call, Throwable t) {}
        });
    }

    private void updateProfile() {
        String name = edtName.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();

        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, String> data = new HashMap<>();
        data.put("KhachHangID", customerUid);
        data.put("Hovaten", name); 
        data.put("Ngaysinh", dob);

        apiService.updateKhachHangProfile(customerUid, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showSuccess();
                } else {
                    Toast.makeText(EditCustomerProfileActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditCustomerProfileActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> edtDob.setText(String.format("%d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String find(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            for (Map.Entry<String, Object> e : m.entrySet()) {
                if (e.getKey().equalsIgnoreCase(k) && e.getValue() != null) return e.getValue().toString();
            }
        }
        return "";
    }

    private void showSuccess() {
        View dv = getLayoutInflater().inflate(R.layout.dialog_success, null);
        AlertDialog d = new AlertDialog.Builder(this).setView(dv).create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.show();
        new Handler().postDelayed(() -> { if (d.isShowing()) { d.dismiss(); setResult(RESULT_OK); finish(); } }, 1500);
    }
}
