package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.UserModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerRegisterActivity extends AppCompatActivity {

    private LinearLayout layoutStepPhone;
    private ScrollView layoutStepForm;
    private EditText edtPhoneInput, edtFullName, edtDob;
    private TextView tvFixedPhone, tvFileName;
    private Button btnVerifyPhone, btnSelectFile, btnFinish, btnCancel;
    
    private String selectedImageBase64 = "";
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageBase64 = encodeImageToBase64(uri);
                        tvFileName.setText("Đã chọn ảnh thành công");
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews();
        setupEvents();
    }

    private void initViews() {
        layoutStepPhone = findViewById(R.id.layoutStepPhone);
        layoutStepForm = findViewById(R.id.layoutStepForm);
        edtPhoneInput = findViewById(R.id.edtPhoneInput);
        btnVerifyPhone = findViewById(R.id.btnVerifyPhone);
        
        edtFullName = findViewById(R.id.edtFullName);
        edtDob = findViewById(R.id.edtDob);
        tvFixedPhone = findViewById(R.id.tvFixedPhone);
        tvFileName = findViewById(R.id.tvFileName);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnFinish = findViewById(R.id.btnFinish);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupEvents() {
        btnVerifyPhone.setOnClickListener(v -> {
            String phone = edtPhoneInput.getText().toString().trim();
            if (phone.length() >= 10) {
                showOtpDialog(phone);
            } else {
                Toast.makeText(this, "SĐT không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        edtDob.setOnClickListener(v -> showDatePicker());

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        btnFinish.setOnClickListener(v -> handleRegister());
        btnCancel.setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showOtpDialog(String phone) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_otp, null);
        b.setView(v);
        final AlertDialog d = b.create();
        d.show();
        
        Button btn = v.findViewById(R.id.btnVerifyOtp);
        if (btn != null) {
            btn.setOnClickListener(v1 -> {
                d.dismiss();
                tvFixedPhone.setText(phone);
                layoutStepPhone.setVisibility(View.GONE);
                layoutStepForm.setVisibility(View.VISIBLE);
            });
        }
    }

    private void handleRegister() {
        String name = edtFullName.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String phone = tvFixedPhone.getText().toString();

        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnFinish.setEnabled(false);

        apiService.getUsers("Get").enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> res) {
                int maxNum = 0;
                boolean phoneExists = false;
                if (res.isSuccessful() && res.body() != null) {
                    for (UserModel u : res.body()) {
                        if (phone.equals(u.getSoDienThoai()) || phone.equals(u.getTenDangNhap())) {
                            phoneExists = true;
                            break;
                        }
                        try {
                            String id = u.getUserID();
                            if (id != null && id.startsWith("US")) {
                                String numOnly = id.substring(2);
                                int n = Integer.parseInt(numOnly);
                                if (n > maxNum) maxNum = n;
                            }
                        } catch (Exception e) {}
                    }
                }
                
                if (phoneExists) {
                    Toast.makeText(CustomerRegisterActivity.this, "Số điện thoại đã được đăng ký!", Toast.LENGTH_LONG).show();
                    btnFinish.setEnabled(true);
                    return;
                }
                
                String nextUsId = String.format("US%05d", maxNum + 1);
                String nextKhId = String.format("KH%05d", maxNum + 1);
                
                createKhachHangProfile(nextUsId, nextKhId, name, dob, phone);
            }
            @Override public void onFailure(Call<List<UserModel>> call, Throwable t) {
                createKhachHangProfile("US00001", "KH00001", name, dob, phone);
            }
        });
    }

    private void createKhachHangProfile(final String usId, final String khId, final String name, String dob, final String phone) {
        Map<String, String> prof = new HashMap<>();
        prof.put("KhachHangID", khId);
        prof.put("Hovaten", name);
        prof.put("Ngaysinh", dob);
        prof.put("AnhDaiDienURL", selectedImageBase64);
        prof.put("Email", "user" + phone + "@nhom7app.com");

        apiService.createKhachHangProfile(prof).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    createAuthAccount(usId, khId, phone, name);
                } else {
                    btnFinish.setEnabled(true);
                    try {
                        String error = response.errorBody().string();
                        Toast.makeText(CustomerRegisterActivity.this, "Lỗi tạo hồ sơ: " + error, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(CustomerRegisterActivity.this, "Lỗi hồ sơ (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                btnFinish.setEnabled(true);
                Toast.makeText(CustomerRegisterActivity.this, "Lỗi mạng bước 1", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAuthAccount(final String usId, final String khId, final String phone, final String name) {
        Map<String, String> auth = new HashMap<>();
        auth.put("UserID", usId);
        auth.put("TenDangNhap", phone);
        auth.put("MatKhau", "123456"); 
        auth.put("SoDienThoai", phone);
        auth.put("Vaitro", "Khachhang");
        auth.put("KhachHang", khId);

        apiService.registerAuth(auth).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnFinish.setEnabled(true);
                if (response.isSuccessful()) {
                    showSuccessPopup(usId, khId, name);
                } else {
                    try {
                        String error = response.errorBody().string();
                        Toast.makeText(CustomerRegisterActivity.this, "Lỗi tài khoản: " + error, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(CustomerRegisterActivity.this, "Lỗi đăng ký!", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                btnFinish.setEnabled(true);
                Toast.makeText(CustomerRegisterActivity.this, "Lỗi mạng bước 2", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessPopup(String usId, String khId, String name) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMsg = dialog.findViewById(R.id.tvMessage);
        if (tvMsg != null) {
            tvMsg.setText("Tạo tài khoản khách hàng\nthành công");
        }

        dialog.show();

        new android.os.Handler().postDelayed(() -> {
            dialog.dismiss();
            saveLoginInfo(usId, khId, name);
            finish();
        }, 2000);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> edtDob.setText(String.format("%d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 15, baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) { return ""; }
    }

    private void saveLoginInfo(String usId, String khId, String name) {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        pref.edit().putBoolean("isLoggedIn", true)
                   .putString("role", "customer")
                   .putString("customerUid", khId)
                   .putString("user_id", usId)
                   .putString("customerName", name)
                   .apply();
    }
}
