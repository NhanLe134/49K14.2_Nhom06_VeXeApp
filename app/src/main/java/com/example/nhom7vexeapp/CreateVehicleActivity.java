package com.example.nhom7vexeapp;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.VehicleManaged;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateVehicleActivity extends AppCompatActivity {

    private EditText edtLicensePlate;
    private Spinner spnCarType, spnStatus;
    private TextView tvSeatCount;
    private Button btnSave, btnCancel;
    private ApiService apiService;
    private List<Loaixe> loaixeList = new ArrayList<>();
    private String nhaXeId;
    private String nextXeID = "XE00001"; // Mặc định nếu chưa có xe nào

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vehicle);

        apiService = ApiClient.getClient().create(ApiService.class);
        
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // ✅ LẤY ID NHÀ XE ĐANG ĐĂNG NHẬP
        nhaXeId = pref.getString("op_uid", "NX00001");

        initViews();
        loadCarTypes();
        setupStatusSpinner();
        calculateNextXeID(); // Lấy danh sách xe để tính ID tiếp theo

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> performSave());
    }

    private void initViews() {
        edtLicensePlate = findViewById(R.id.edtLicensePlate);
        spnCarType = findViewById(R.id.spnCarType);
        spnStatus = findViewById(R.id.spnStatus);
        tvSeatCount = findViewById(R.id.tvSeatCount);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void calculateNextXeID() {
        apiService.getVehicles().enqueue(new Callback<List<VehicleManaged>>() {
            @Override
            public void onResponse(Call<List<VehicleManaged>> call, Response<List<VehicleManaged>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Integer> ids = new ArrayList<>();
                    for (VehicleManaged v : response.body()) {
                        try {
                            String xeIdStr = v.getXeID();
                            if (xeIdStr != null && xeIdStr.startsWith("XE")) {
                                String numericPart = xeIdStr.substring(2);
                                ids.add(Integer.parseInt(numericPart));
                            }
                        } catch (Exception e) {}
                    }
                    if (!ids.isEmpty()) {
                        int maxId = Collections.max(ids);
                        nextXeID = String.format("XE%05d", maxId + 1);
                    }
                }
            }
            @Override public void onFailure(Call<List<VehicleManaged>> call, Throwable t) {}
        });
    }

    private void loadCarTypes() {
        apiService.getLoaixe().enqueue(new Callback<List<Loaixe>>() {
            @Override
            public void onResponse(Call<List<Loaixe>> call, Response<List<Loaixe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loaixeList = response.body();
                    List<String> names = new ArrayList<>();
                    for (Loaixe l : loaixeList) {
                        names.add(l.getLoaixeID());
                    }
                    
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateVehicleActivity.this, 
                        android.R.layout.simple_spinner_dropdown_item, names);
                    spnCarType.setAdapter(adapter);

                    spnCarType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            tvSeatCount.setText(String.valueOf(loaixeList.get(position).getSoCho()));
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }
            @Override public void onFailure(Call<List<Loaixe>> call, Throwable t) {}
        });
    }

    private void setupStatusSpinner() {
        String[] statuses = {"Đang hoạt động", "Bảo trì", "Dừng hoạt động"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, statuses);
        spnStatus.setAdapter(adapter);
    }

    private void performSave() {
        String plate = edtLicensePlate.getText().toString().trim();
        if (plate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập biển số xe", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = spnCarType.getSelectedItemPosition();
        if (pos < 0) return;
        
        String carTypeId = loaixeList.get(pos).getLoaixeID();
        String status = spnStatus.getSelectedItem().toString();
        int seatCount = 0;
        try {
            seatCount = Integer.parseInt(tvSeatCount.getText().toString());
        } catch (Exception e) {}

        // ✅ GỬI ĐÚNG ID NHÀ XE ĐANG ĐĂNG NHẬP
        Map<String, Object> data = new HashMap<>();
        data.put("XeID", nextXeID); 
        data.put("BienSoXe", plate);
        data.put("Nhaxe", nhaXeId); // Mã nhà xe lấy từ SharedPreferences (op_uid)
        data.put("Loaixe", carTypeId);
        data.put("TrangThai", status);
        data.put("SoGhe", seatCount);

        apiService.createVehicle(data).enqueue(new Callback<Void>() {
             @Override
             public void onResponse(Call<Void> call, Response<Void> response) {
                 if (response.isSuccessful()) {
                     showSuccessPopup();
                 } else {
                     try {
                         String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown Error";
                         Toast.makeText(CreateVehicleActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                     } catch (Exception e) {
                         Toast.makeText(CreateVehicleActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                     }
                 }
             }
             @Override public void onFailure(Call<Void> call, Throwable t) {
                 Toast.makeText(CreateVehicleActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
             }
        });
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
            tvMsg.setText("Thêm thông tin phương tiện thành công");
        }

        dialog.show();

        new android.os.Handler().postDelayed(() -> {
            dialog.dismiss();
            setResult(RESULT_OK);
            finish();
        }, 2000);
    }
}
