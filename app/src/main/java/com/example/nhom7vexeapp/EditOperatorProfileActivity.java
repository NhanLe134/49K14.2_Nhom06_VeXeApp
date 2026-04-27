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

// #Trang
public class EditOperatorProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditOpProfile";
    private EditText edtName, edtRep, edtAddress, edtPhone, edtEmail;
    private TextView tvErrorName, tvFileName;
    private MaterialButton btnSave, btnCancel, btnSelectFile;
    private ImageView imgPreview;
    private String opUid; 
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

        findViewById(R.id.btnBack).setOnClickListener(v -> showCancelConfirmPopup());
        btnCancel.setOnClickListener(v -> showCancelConfirmPopup());

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                handleSmartUpdate();
            }
        });
    }

    private void showCancelConfirmPopup() {
        View dv = getLayoutInflater().inflate(R.layout.dialog_confirm_cancel, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        dv.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());
        dv.findViewById(R.id.btnYes).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        
        dialog.show();
    }

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
        edtEmail = findViewById(R.id.edtEditOpEmail);
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
                    edtEmail.setText(data.getEmail());
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

        Map<String, Object> data = new HashMap<>();
        data.put("NhaxeID", opUid);
        data.put("Tennhaxe", edtName.getText().toString().trim()); 
        data.put("TenNguoiDaiDien", edtRep.getText().toString().trim());
        data.put("Email", edtEmail.getText().toString().trim());
        data.put("AnhDaiDien", selectedImageBase64); 
        data.put("DiaChiTruSo", edtAddress.getText().toString().trim());
        data.put("SoDienThoai", edtPhone.getText().toString().trim());

        apiService.patchNhaXeProfile(opUid, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showSuccessPopup("Cập nhật thông tin nhà xe thành công");
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

    private void showSuccessPopup(String message) {
        View dv = getLayoutInflater().inflate(R.layout.dialog_update_success, null);
        TextView tvMsg = dv.findViewById(R.id.tvSuccessMessage);
        if (tvMsg != null) tvMsg.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
}
