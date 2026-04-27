package com.example.nhom7vexeapp;

import android.app.Dialog;
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
import android.view.View;
import android.view.Window;
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
    private String selectedImageBase64 = "";

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        if (imgPreview != null) imgPreview.setImageURI(uri);
                        selectedImageBase64 = encodeImageToBase64(uri);
                        if (tvFileName != null) tvFileName.setText("Đã chọn ảnh thành công");
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
        
        // Cập nhật nút Hủy để hiển thị popup xác nhận
        View btnCancel = findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> showConfirmCancelDialog());
        }
        
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

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
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

        Map<String, String> authMap = new HashMap<>();
        authMap.put("UserID", randomID);
        authMap.put("TenDangNhap", user);
        authMap.put("MatKhau", pass);
        authMap.put("SoDienThoai", phone);
        authMap.put("Vaitro", "Nhaxe");

        btnRegister.setEnabled(false);

        apiService.registerAuth(authMap).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> rA) {
                if (rA.isSuccessful()) {
                    Map<String, String> profileData = new HashMap<>();
                    profileData.put("NhaxeID", randomID);
                    profileData.put("Tennhaxe", opName);
                    profileData.put("TenNguoiDaiDien", edtRepName.getText().toString().trim());
                    profileData.put("Email", user + "@gmail.com");
                    profileData.put("DiaChiTruSo", edtAddress.getText().toString().trim());
                    profileData.put("SoDienThoai", phone);
                    profileData.put("AnhDaiDien", selectedImageBase64);

                    apiService.createNhaXeProfile(profileData).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> rP) {
                            btnRegister.setEnabled(true);
                            if (rP.isSuccessful()) {
                                showSuccessPopup();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Lỗi tạo hồ sơ!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {
                            btnRegister.setEnabled(true);
                        }
                    });
                } else {
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Tên đăng nhập hoặc SĐT đã tồn tại!", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                btnRegister.setEnabled(true);
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
            tvMsg.setText("Tạo tài khoản thành công.\nNhấn vào màn hình để quay lại trang đăng nhập.");
        }

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        
        // Khi người dùng chạm vào màn hình/dialog thì thoát về màn hình đăng nhập
        dialog.setOnDismissListener(d -> finish());

        dialog.show();

        // Tự động đóng sau 3 giây nếu không chạm
        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 3000);
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

        if (tvMsg != null) {
            tvMsg.setText("Bạn chắc chắn muốn hủy đăng ký tài khoản?");
        }

        if (btnNo != null) btnNo.setOnClickListener(v -> dialog.dismiss());
        if (btnYes != null) btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
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
