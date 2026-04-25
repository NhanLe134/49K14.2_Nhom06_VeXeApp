package com.example.nhom7vexeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtOperatorName, edtRepName, edtUsername, edtPassword, edtConfirmPassword, edtAddress, edtPhone;
    private TextView tvErrorOperatorName, tvErrorUsername, tvErrorPassword, tvFileName;
    private Button btnRegister, btnCancel, btnVerifyPhone, btnSelectFile;
    private boolean isOtpVerified = false;
    private String selectedImageBase64 = ""; 

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageBase64 = encodeImageToBase64(uri);
                        tvFileName.setText("Đã chọn ảnh");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> { if (validateForm()) handleRegister(); });

        btnVerifyPhone.setOnClickListener(v -> showOtpDialog());
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

    private void initViews() {
        edtOperatorName = findViewById(R.id.edtOperatorName);
        edtRepName = findViewById(R.id.edtRepName);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        tvErrorOperatorName = findViewById(R.id.tvErrorOperatorName);
        tvErrorUsername = findViewById(R.id.tvErrorUsername);
        tvErrorPassword = findViewById(R.id.tvErrorPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnCancel = findViewById(R.id.btnCancel);
        btnVerifyPhone = findViewById(R.id.btnVerifyPhone);
        btnSelectFile = findViewById(R.id.btnSelectOpFile);
        tvFileName = findViewById(R.id.tvOpFileName);
    }

    private void handleRegister() {
        String username = edtUsername.getText().toString().trim();
        String operatorName = edtOperatorName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String randomUserID = "NX" + (10000 + new Random().nextInt(89999));

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // BƯỚC 1: Đăng ký tài khoản Auth
        Map<String, String> authData = new HashMap<>();
        authData.put("UserID", randomUserID);
        authData.put("TenDangNhap", username);
        authData.put("MatKhau", edtPassword.getText().toString().trim());
        authData.put("SoDienThoai", phone);
        authData.put("Vaitro", "Nhaxe");

        apiService.registerAuth(authData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // BƯỚC 2: Tạo Profile Nhà xe
                    Map<String, String> profileData = new HashMap<>();
                    profileData.put("NhaxeID", randomUserID);
                    profileData.put("Tennhaxe", operatorName); // KHỚP 100% DATABASE (h và x thường)
                    profileData.put("Email", username + "@gmail.com");
                    profileData.put("AnhDaiDienURL", selectedImageBase64);
                    profileData.put("DiaChiTruSo", edtAddress.getText().toString().trim());
                    profileData.put("SoDienThoai", phone);

                    apiService.createNhaXeProfile(profileData).enqueue(new Callback<Void>() {
                        @Override public void onResponse(Call<Void> call, Response<Void> res) {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(RegisterActivity.this, "Lỗi lưu thông tin nhà xe!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Tài khoản hoặc SĐT đã tồn tại!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { Toast.makeText(RegisterActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void showOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_otp, null);
        builder.setView(view);
        Button btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp);
        btnVerifyOtp.setVisibility(View.VISIBLE);
        AlertDialog dialog = builder.create();
        dialog.show();
        btnVerifyOtp.setOnClickListener(v -> {
            isOtpVerified = true;
            btnRegister.setEnabled(true);
            btnRegister.setAlpha(1.0f);
            dialog.dismiss();
        });
    }

    private boolean validateForm() {
        if (edtOperatorName.getText().toString().trim().isEmpty()) return false;
        if (!isOtpVerified) { Toast.makeText(this, "Vui lòng xác thực OTP", Toast.LENGTH_SHORT).show(); return false; }
        return true;
    }
}
