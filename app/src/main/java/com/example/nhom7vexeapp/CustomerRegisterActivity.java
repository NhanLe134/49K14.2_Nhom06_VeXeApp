package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.utils.IdGenerator;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerRegisterActivity extends AppCompatActivity {

    private LinearLayout layoutStepPhone;
    private ScrollView layoutStepForm;
    private EditText edtPhoneInput, edtFullName, edtDob, edtEmail;
    private TextView tvFixedPhone, tvFileName, tvHeaderTitle;
    private MaterialButton btnVerifyPhone, btnFinish, btnSelectFile;
    private String verifiedPhone = "";
    private Uri selectedImageUri = null;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                tvFileName.setText("Đã chọn ảnh đại diện");
            }
        });

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
        edtEmail = findViewById(R.id.edtEmailInput); // Giả định ID trong XML
        tvFixedPhone = findViewById(R.id.tvFixedPhone);
        tvFileName = findViewById(R.id.tvFileName);
        btnFinish = findViewById(R.id.btnFinish);
        btnSelectFile = findViewById(R.id.btnSelectFile);
    }

    private void setupEvents() {
        btnVerifyPhone.setOnClickListener(v -> {
            String phone = edtPhoneInput.getText().toString().trim();
            if (phone.length() >= 10) {
                verifiedPhone = phone;
                showOtpDialog();
            } else {
                Toast.makeText(this, "SĐT không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        btnSelectFile.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        btnFinish.setOnClickListener(v -> {
            if (validateForm()) {
                saveToDatabaseAndFinish();
            }
        });
    }

    private void saveToDatabaseAndFinish() {
        // 1. Sinh ID tự động
        String newId = IdGenerator.generateKhachHangID(this);
        
        // 2. Chuẩn bị dữ liệu dạng RequestBody (Cho Multipart)
        RequestBody idBody = RequestBody.create(MediaType.parse("text/plain"), newId);
        RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), edtFullName.getText().toString().trim());
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), edtEmail != null ? edtEmail.getText().toString() : "");
        RequestBody dobBody = RequestBody.create(MediaType.parse("text/plain"), edtDob.getText().toString().trim());

        // 3. Chuẩn bị File Ảnh
        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File file = uriToFile(selectedImageUri);
            if (file != null) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                imagePart = MultipartBody.Part.createFormData("AnhDaiDienURL", file.getName(), requestFile);
            }
        }

        // 4. Gọi API lưu vào Database Render
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.createKhachHangProfile(idBody, nameBody, emailBody, dobBody, imagePart).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CustomerRegisterActivity.this, "Đã lưu dữ liệu vào Database Render!", Toast.LENGTH_LONG).show();
                    finish(); // Quay về login
                } else {
                    Toast.makeText(CustomerRegisterActivity.this, "Lỗi Server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CustomerRegisterActivity.this, "Lỗi kết nối Render: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "temp_avatar.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    // Các hàm phụ trợ khác (showOtpDialog, validateForm...) giữ nguyên như cũ
    private void showOtpDialog() {
        moveToForm(); // Giả định xác thực xong để demo nhanh
    }

    private void moveToForm() {
        layoutStepPhone.setVisibility(View.GONE);
        layoutStepForm.setVisibility(View.VISIBLE);
        tvFixedPhone.setText(verifiedPhone);
    }

    private boolean validateForm() {
        return !edtFullName.getText().toString().isEmpty() && !edtDob.getText().toString().isEmpty();
    }
}
