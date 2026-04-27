package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.VehicleManaged;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditVehicleActivity extends AppCompatActivity {

    private TextView tvPlate, tvType, tvSeats, tvStatusDisplay;
    private ImageView btnBack;
    private MaterialButton btnSave, btnCancel;
    private LinearLayout layoutStatus;
    private VehicleManaged currentVehicle;
    private ApiService apiService;
    private String selectedStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews();

        if (getIntent() != null && getIntent().hasExtra("vehicle_managed_data")) {
            currentVehicle = (VehicleManaged) getIntent().getSerializableExtra("vehicle_managed_data");
            if (currentVehicle != null) {
                loadInitialData();
            }
        }

        btnBack.setOnClickListener(v -> showCancelConfirmationDialog());
        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());
        layoutStatus.setOnClickListener(v -> showStatusSelectionDialog());

        // LOGIC LƯU VÀO DATABASE RENDER
        btnSave.setOnClickListener(v -> saveChangesToDatabase());
    }

    private void initViews() {
        tvPlate = findViewById(R.id.tvEditPlate);
        tvType = findViewById(R.id.tvEditType);
        tvSeats = findViewById(R.id.tvEditSeats);
        tvStatusDisplay = findViewById(R.id.tvStatusDisplay);
        layoutStatus = findViewById(R.id.layoutStatus);
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSaveEdit);
        btnCancel = findViewById(R.id.btnCancelEdit);
    }

    private void loadInitialData() {
        tvPlate.setText(currentVehicle.getBienSoXe());
        selectedStatus = currentVehicle.getTrangThai();
        tvStatusDisplay.setText(selectedStatus);

        apiService.getLoaixe().enqueue(new Callback<List<Loaixe>>() {
            @Override
            public void onResponse(Call<List<Loaixe>> call, Response<List<Loaixe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String targetTypeId = currentVehicle.getLoaiXeIDStr();
                    for (Loaixe type : response.body()) {
                        if (type.getLoaixeID().equalsIgnoreCase(targetTypeId)) {
                            tvType.setText("Xe " + type.getSoCho() + " chỗ");
                            tvSeats.setText(String.valueOf(type.getSoCho()));
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<Loaixe>> call, Throwable t) {}
        });
    }

    private void saveChangesToDatabase() {
        if (currentVehicle == null || selectedStatus.isEmpty()) return;

        // KHỚP VỚI DATABASE DJANGO: Cập nhật trường TrangThai của bảng Xe
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("TrangThai", selectedStatus);

        apiService.patchVehicle(currentVehicle.getXeID(), updateData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showSuccessPopup();
                } else {
                    Toast.makeText(EditVehicleActivity.this, "Lỗi cập nhật server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditVehicleActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStatusSelectionDialog() {
        String[] statuses = {"Đang hoạt động", "Bảo trì", "Dừng hoạt động"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_status, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogView.findViewById(R.id.tvStatusOpt1).setOnClickListener(v -> { selectedStatus = statuses[0]; tvStatusDisplay.setText(selectedStatus); dialog.dismiss(); });
        dialogView.findViewById(R.id.tvStatusOpt2).setOnClickListener(v -> { selectedStatus = statuses[1]; tvStatusDisplay.setText(selectedStatus); dialog.dismiss(); });
        dialogView.findViewById(R.id.tvStatusOpt3).setOnClickListener(v -> { selectedStatus = statuses[2]; tvStatusDisplay.setText(selectedStatus); dialog.dismiss(); });
    }

    private void showCancelConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cancel_confirmation, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogView.findViewById(R.id.btnDialogNo).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDialogYes).setOnClickListener(v -> { dialog.dismiss(); finish(); });
    }

    private void showSuccessPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_success, null);
        TextView tvMsg = dialogView.findViewById(R.id.tvMessage);
        if (tvMsg != null) tvMsg.setText("Đã cập nhật thông tin xe thành công.");
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) dialog.dismiss();
            setResult(RESULT_OK);
            finish();
        }, 1500);
    }
}
