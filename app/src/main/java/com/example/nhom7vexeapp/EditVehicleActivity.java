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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nhom7vexeapp.models.Vehicle;
import com.google.android.material.button.MaterialButton;

public class EditVehicleActivity extends AppCompatActivity {

    private TextView tvPlate, tvType, tvSeats, tvStatusDisplay;
    private ImageView btnBack;
    private MaterialButton btnSave, btnCancel;
    private LinearLayout layoutStatus;
    private String selectedStatus = "Hoạt động";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

        initViews();

        // Nhận dữ liệu từ Intent
        Vehicle vehicle = (Vehicle) getIntent().getSerializableExtra("vehicle_data");
        if (vehicle != null) {
            tvPlate.setText(vehicle.getPlateNumber());
            tvType.setText(vehicle.getType());
            tvSeats.setText(String.valueOf(vehicle.getSeatCount()));
            selectedStatus = vehicle.getStatus();
            tvStatusDisplay.setText(selectedStatus);
        }

        btnBack.setOnClickListener(v -> showCancelConfirmationDialog());
        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());

        layoutStatus.setOnClickListener(v -> showStatusSelectionDialog());

        btnSave.setOnClickListener(v -> {
            // Logic lưu thông tin (ở đây chỉ hiển thị popup thành công)
            showSuccessPopup();
        });
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

    private void showCancelConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_confirmation, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }

        MaterialButton btnNo = dialogView.findViewById(R.id.btnDialogNo);
        MaterialButton btnYes = dialogView.findViewById(R.id.btnDialogYes);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
    }

    private void showStatusSelectionDialog() {
        String[] statuses = {"Hoạt động", "Đang bảo trì", "Tạm dừng"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_status, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(lp);
        }

        dialogView.findViewById(R.id.tvStatusOpt1).setOnClickListener(v -> {
            tvStatusDisplay.setText(statuses[0]);
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.tvStatusOpt2).setOnClickListener(v -> {
            tvStatusDisplay.setText(statuses[1]);
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.tvStatusOpt3).setOnClickListener(v -> {
            tvStatusDisplay.setText(statuses[2]);
            dialog.dismiss();
        });
    }

    private void showSuccessPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_success, null);
        
        TextView tvMessage = dialogView.findViewById(R.id.tvSuccessMessage);
        if (tvMessage != null) {
            tvMessage.setText("Cập nhật thông tin Nhà xe thành công");
        }
        
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            window.setAttributes(layoutParams);
        }
        
        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Intent intent = new Intent(EditVehicleActivity.this, QLPhuongTienActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, 1500);
    }
}
