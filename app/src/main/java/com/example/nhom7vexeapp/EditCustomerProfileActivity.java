package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        
        String savedPhone = pref.getString("customerPhone", "");
        if (!savedPhone.isEmpty()) tvPhone.setText(savedPhone);
        
        loadData();

        edtDob.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> updateProfile());
        
        // Cập nhật sự kiện nút Hủy để hiện popup xác nhận
        btnCancel.setOnClickListener(v -> showConfirmCancelDialog());
        
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
                    showSuccessPopup();
                } else {
                    Toast.makeText(EditCustomerProfileActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditCustomerProfileActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmCancelDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_cancel);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMsg = dialog.findViewById(R.id.tvDialogMessage);
        Button btnNo = dialog.findViewById(R.id.btnNo);
        Button btnYes = dialog.findViewById(R.id.btnYes);

        if (tvMsg != null) tvMsg.setText("Bạn có chắc muốn hủy thao tác này?");

        if (btnNo != null) {
            btnNo.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnYes != null) {
            btnYes.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
        }

        dialog.show();
    }

    private void showSuccessPopup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMsg = dialog.findViewById(R.id.tvMessage);
        if (tvMsg != null) {
            tvMsg.setText("Cập nhật thông tin khách hàng\nthành công");
        }

        dialog.show();

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        }, 2000);
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
}
