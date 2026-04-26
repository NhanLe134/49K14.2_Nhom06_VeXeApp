package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtOperatorName, edtRepName, edtUsername, edtPassword, edtConfirmPassword, edtAddress, edtPhone;
    private TextView tvFileName;
    private Button btnRegister, btnVerifyPhone, btnSelectFile;
    private ImageView imgPreview;
    private boolean isOtpVerified = false;
    private String selectedImageBase64 = ""; // Dùng chuỗi Base64 thay vì File

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        if (imgPreview != null) imgPreview.setImageURI(uri);
                        selectedImageBase64 = encodeImageToBase64(uri);
                        if (tvFileName != null) tvFileName.setText("Đã chọn ảnh & mã hóa thành công");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();

        if (btnSelectFile != null) {
            btnSelectFile.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            });
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        
        btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                handleRegister();
            } else if (!isOtpVerified) {
                Toast.makeText(this, "Vui lòng 'Xác nhận' OTP trước!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            }
        });

        btnVerifyPhone.setOnClickListener(v -> showOtpDialog());
    }

    // Hàm mã hóa ảnh sang Base64
    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Nén ảnh xuống 30% để giảm kích thước chuỗi Base64
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void initViews() {
        edtOperatorName = findViewById(R.id.edtOperatorName);
        edtRepName = findViewById(R.id.edtRepName);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);
        btnVerifyPhone = findViewById(R.id.btnVerifyPhone);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        tvFileName = findViewById(R.id.tvFileName);
        imgPreview = findViewById(R.id.imgRegPreview);
    }

    private void handleRegister() {
        final String user = edtUsername.getText().toString().trim();
        final String pass = edtPassword.getText().toString().trim();
        final String opName = edtOperatorName.getText().toString().trim();
        final String phone = edtPhone.getText().toString().trim();
        final String randomID = "NX" + (10000 + new Random().nextInt(89999));

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // BƯỚC 1: TẠO TÀI KHOẢN AUTH
        Map<String, String> authMap = new HashMap<>();
        authMap.put("UserID", randomID);
        authMap.put("TenDangNhap", user);
        authMap.put("MatKhau", pass);
        authMap.put("SoDienThoai", phone);
        authMap.put("Vaitro", "Nhaxe");

        Toast.makeText(this, "Đang đăng ký...", Toast.LENGTH_SHORT).show();

        apiService.registerAuth(authMap).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> rA) {
                if (rA.isSuccessful()) {
                    // BƯỚC 2: TẠO PROFILE (GỬI JSON CHỨA CHUỖI BASE64)
                    Map<String, String> profileData = new HashMap<>();
                    profileData.put("NhaxeID", randomID);
                    profileData.put("Tennhaxe", opName);
                    profileData.put("TenNguoiDaiDien", edtRepName.getText().toString().trim());
                    profileData.put("Email", user + "@gmail.com");
                    profileData.put("DiaChiTruSo", edtAddress.getText().toString().trim());
                    profileData.put("SoDienThoai", phone);
                    profileData.put("AnhDaiDien", selectedImageBase64); // Gửi chuỗi Base64

                    apiService.createNhaXeProfile(profileData).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> rP) {
                            if (rP.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                saveLoginInfo(randomID, user);
                            } else {
                                try {
                                    Toast.makeText(RegisterActivity.this, "Lỗi Profile: " + rP.errorBody().string(), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {}
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(RegisterActivity.this, "Lỗi mạng bước 2!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Lỗi: SĐT hoặc Tên đăng nhập đã tồn tại!", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLoginInfo(String id, String user) {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", "operator");
        editor.putString("op_uid", id);
        editor.putString("op_user", user);
        editor.apply();

        startActivity(new Intent(this, OperatorMainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void showOtpDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_otp, null);
        b.setView(v);
        final AlertDialog d = b.create();
        d.show();
        Button btn = v.findViewById(R.id.btnVerifyOtp);
        if (btn != null) {
            btn.setOnClickListener(v1 -> {
                isOtpVerified = true;
                Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                d.dismiss();
            });
        }
    }

    private boolean validateForm() {
        return !edtOperatorName.getText().toString().trim().isEmpty() && isOtpVerified;
    }
}
