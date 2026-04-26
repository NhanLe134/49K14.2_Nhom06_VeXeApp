package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditOperatorProfileActivity extends AppCompatActivity {

    private EditText edtName, edtRep, edtAddress, edtPhone;
    private TextView tvErrorName, tvFileName;
    private MaterialButton btnSave, btnCancel, btnSelectFile;
    private ImageView imgPreview;
    private String opUid, opEmail = ""; 
    private String selectedImageBase64 = "";

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        imgPreview.setImageURI(uri);
                        selectedImageBase64 = encodeImageToBase64(uri);
                        tvFileName.setText("Đã chọn ảnh mới");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_operator_profile);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        opUid = pref.getString("op_uid", "");

        initViews();
        loadCurrentDataFromDB();
        setupBottomNavigation();

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                handleSmartUpdate();
            }
        });
    }

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Nén ảnh xuống 30% để chuỗi Base64 không quá dài
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) { return ""; }
    }

    private void initViews() {
        edtName = findViewById(R.id.edtEditOpName);
        edtRep = findViewById(R.id.edtEditOpRep);
        edtAddress = findViewById(R.id.edtEditOpAddress);
        edtPhone = findViewById(R.id.edtEditOpPhone);
        tvErrorName = findViewById(R.id.tvErrorOpName);
        btnSave = findViewById(R.id.btnSaveEditOp);
        btnCancel = findViewById(R.id.btnCancelEditOp);
        btnSelectFile = findViewById(R.id.btnSelectOpFile);
        tvFileName = findViewById(R.id.tvOpFileName);
        imgPreview = findViewById(R.id.imgEditOpPreview);
    }

    private void loadCurrentDataFromDB() {
        if (opUid.isEmpty()) return;
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getNhaXeDetail(opUid).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    edtName.setText(findValue(data, "Tennhaxe", "TenNhaXe"));
                    edtRep.setText(findValue(data, "TenNguoiDaiDien", "NguoiDaiDien"));
                    edtAddress.setText(findValue(data, "DiaChiTruSo", "Diachitruso"));
                    edtPhone.setText(findValue(data, "SoDienThoai", "Sodienthoai"));
                    opEmail = findValue(data, "Email", "email");
                    String imgData = findValue(data, "AnhDaiDien", "AnhDaiDienURL");
                    if (!imgData.isEmpty()) {
                        Glide.with(EditOperatorProfileActivity.this).load(imgData).into(imgPreview);
                        selectedImageBase64 = imgData;
                    }
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private void handleSmartUpdate() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Toast.makeText(this, "Đang cập nhật...", Toast.LENGTH_SHORT).show();

        Map<String, String> data = new HashMap<>();
        data.put("NhaxeID", opUid);
        data.put("Tennhaxe", edtName.getText().toString().trim()); 
        data.put("TenNguoiDaiDien", edtRep.getText().toString().trim());
        data.put("Email", opEmail.isEmpty() ? "nhaxe@gmail.com" : opEmail);
        data.put("AnhDaiDien", selectedImageBase64); 
        data.put("DiaChiTruSo", edtAddress.getText().toString().trim());
        data.put("SoDienThoai", edtPhone.getText().toString().trim());

        apiService.updateNhaXeProfile(opUid, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showSuccessPopup();
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Lỗi";
                        Toast.makeText(EditOperatorProfileActivity.this, "Server báo lỗi: " + err, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {}
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditOperatorProfileActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String findValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null && !map.get(key).toString().equals("null")) return map.get(key).toString();
        }
        return "";
    }

    private void setupBottomNavigation() {
        View h = findViewById(R.id.nav_home_op_main); if (h == null) h = findViewById(R.id.navHomeEditProfile);
        if (h != null) h.setOnClickListener(v -> { startActivity(new Intent(this, OperatorMainActivity.class)); finish(); });
        View t = findViewById(R.id.nav_trip_op); if (t != null) t.setOnClickListener(v -> { startActivity(new Intent(this, TripListActivity.class)); finish(); });
        View r = findViewById(R.id.nav_route_op); if (r != null) r.setOnClickListener(v -> { startActivity(new Intent(this, QLTuyenxeActivity.class)); finish(); });
        View v = findViewById(R.id.nav_vehicle_op); if (v != null) v.setOnClickListener(v1 -> { startActivity(new Intent(this, PhuongTienManagementActivity.class)); finish(); });
    }

    private boolean validateForm() { return !edtName.getText().toString().trim().isEmpty(); }

    private void showSuccessPopup() {
        View dv = getLayoutInflater().inflate(R.layout.dialog_update_success, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(() -> { if (dialog.isShowing()) { dialog.dismiss(); setResult(RESULT_OK); finish(); } }, 1500);
    }
}
