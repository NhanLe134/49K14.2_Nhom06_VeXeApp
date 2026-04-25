package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                handleUpdate();
            }
        });
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
                    // SỬA LẠI: Khớp key Tennhaxe từ Database
                    edtName.setText(findValue(data, "Tennhaxe", "TenNhaXe"));
                    edtRep.setText(findValue(data, "NguoiDaiDien", "Nguoidaidien"));
                    edtAddress.setText(findValue(data, "DiaChiTruSo", "Diachitruso"));
                    edtPhone.setText(findValue(data, "SoDienThoai", "Sodienthoai"));
                    opEmail = findValue(data, "Email", "email");

                    String imgUrl = findValue(data, "AnhDaiDienURL", "Anhdaidienurl");
                    if (!imgUrl.isEmpty()) {
                        Glide.with(EditOperatorProfileActivity.this).load(imgUrl).into(imgPreview);
                        selectedImageBase64 = imgUrl;
                    }
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });
    }

    private String findValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) return map.get(key).toString();
        }
        return "";
    }

    private void handleUpdate() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, String> data = new HashMap<>();
        data.put("NhaxeID", opUid);
        data.put("Tennhaxe", edtName.getText().toString().trim()); // SỬA KEY cho khớp Database
        data.put("NguoiDaiDien", edtRep.getText().toString().trim());
        data.put("DiaChiTruSo", edtAddress.getText().toString().trim());
        data.put("SoDienThoai", edtPhone.getText().toString().trim());
        data.put("Email", opEmail.isEmpty() ? "nhaxe@gmail.com" : opEmail);
        data.put("AnhDaiDienURL", selectedImageBase64);

        apiService.updateNhaXeProfile(opUid, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showSuccessPopup();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Lỗi";
                        Toast.makeText(EditOperatorProfileActivity.this, "Lỗi Server: " + error, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(EditOperatorProfileActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditOperatorProfileActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            android.graphics.Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 40, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return "data:image/jpeg;base64," + android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
        } catch (Exception e) { return ""; }
    }

    private boolean validateForm() {
        if (edtName.getText().toString().trim().isEmpty()) return false;
        return true;
    }

    private void showSuccessPopup() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_success, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(() -> { if (dialog.isShowing()) { dialog.dismiss(); setResult(RESULT_OK); finish(); } }, 1500);
    }
}
