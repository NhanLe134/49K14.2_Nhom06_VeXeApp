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
import android.util.Log;
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
import com.example.nhom7vexeapp.models.NhaXe;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditOperatorProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditOpProfile";
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
            // Nén ảnh xuống 40% để đảm bảo độ dài chuỗi Base64 hợp lý cho server
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) { 
            Log.e(TAG, "Error encoding image: " + e.getMessage());
            return ""; 
        }
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
        apiService.getNhaXeDetail(opUid).enqueue(new Callback<NhaXe>() {
            @Override
            public void onResponse(Call<NhaXe> call, Response<NhaXe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NhaXe data = response.body();
                    edtName.setText(data.getBusName());
                    edtRep.setText(data.getRepresentative());
                    edtAddress.setText(data.getAddress());
                    edtPhone.setText(data.getPhone());
                    opEmail = data.getEmail();
                    String imgData = data.getBannerUrl();
                    if (imgData != null && !imgData.isEmpty()) {
                        Glide.with(EditOperatorProfileActivity.this)
                             .load(imgData)
                             .placeholder(R.drawable.nhaxe_home)
                             .into(imgPreview);
                        selectedImageBase64 = imgData;
                    }
                }
            }
            @Override public void onFailure(Call<NhaXe> call, Throwable t) {
                Toast.makeText(EditOperatorProfileActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSmartUpdate() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Toast.makeText(this, "Đang cập nhật...", Toast.LENGTH_SHORT).show();

        Map<String, String> data = new HashMap<>();
        data.put("NhaxeID", opUid);
        data.put("Tennhaxe", edtName.getText().toString().trim()); 
        data.put("TenNguoiDaiDien", edtRep.getText().toString().trim());
        data.put("Email", (opEmail == null || opEmail.isEmpty()) ? "nhaxe@gmail.com" : opEmail);
        
        // Dựa trên ảnh Admin bạn gửi, key đúng phải là "AnhDaiDien"
        data.put("AnhDaiDien", selectedImageBase64); 
        
        data.put("DiaChiTruSo", edtAddress.getText().toString().trim());
        data.put("SoDienThoai", edtPhone.getText().toString().trim());

        apiService.patchNhaXeProfile(opUid, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showSuccessPopup();
                } else {
                    Log.e(TAG, "Update failed: " + response.code());
                    Toast.makeText(EditOperatorProfileActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network Error: " + t.getMessage());
                Toast.makeText(EditOperatorProfileActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        View h = findViewById(R.id.navHomeEditProfile);
        if (h != null) h.setOnClickListener(v -> { 
            startActivity(new Intent(this, OperatorMainActivity.class)); 
            finish(); 
        });
    }

    private boolean validateForm() { 
        if (edtName.getText().toString().trim().isEmpty()) {
            tvErrorName.setVisibility(View.VISIBLE);
            return false;
        }
        tvErrorName.setVisibility(View.GONE);
        return true; 
    }

    private void showSuccessPopup() {
        View dv = getLayoutInflater().inflate(R.layout.dialog_update_success, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(() -> { 
            if (dialog.isShowing()) { 
                dialog.dismiss(); 
                setResult(RESULT_OK); 
                finish(); 
            } 
        }, 1500);
    }
}
