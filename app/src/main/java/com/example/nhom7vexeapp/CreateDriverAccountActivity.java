package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.ChiTietTaiXeModel;
import com.example.nhom7vexeapp.models.TaixeModel;
import com.example.nhom7vexeapp.models.UserModel;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateDriverAccountActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword, edtConfirmPassword, edtFullName, edtPhone, edtCCCD, edtLicense, edtLicenseType, edtExpiryDate;
    private TextView tvErrorUsername, tvErrorPassword, tvErrorConfirmPassword, tvErrorFullName, tvErrorPhone, tvErrorCCCD, tvErrorLicense, tvErrorLicenseType, tvErrorExpiryDate;
    private Button btnSubmit, btnSelectImage;
    private TextView tvImageName;
    private ImageView btnClose;
    private ApiService apiService;
    private String currentNhaxeId;
    private String selectedImageBase64 = "";

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    tvImageName.setText("Đã chọn ảnh mới");
                    selectedImageBase64 = encodeImageToBase64(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_driver_account);

        apiService = ApiClient.getClient().create(ApiService.class);
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentNhaxeId = pref.getString("nhaxe_id", "NX001");

        initViews();

        btnClose.setOnClickListener(v -> finish());
        edtExpiryDate.setOnClickListener(v -> showDatePicker());
        btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                checkDuplicatesAndCreate();
            }
        });
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtCCCD = findViewById(R.id.edtCCCD);
        edtLicense = findViewById(R.id.edtLicense);
        edtLicenseType = findViewById(R.id.edtLicenseType);
        edtExpiryDate = findViewById(R.id.edtExpiryDate);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnClose = findViewById(R.id.btnClose);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        tvImageName = findViewById(R.id.tvImageName);

        tvErrorUsername = findViewById(R.id.tvErrorUsername);
        tvErrorPassword = findViewById(R.id.tvErrorPassword);
        tvErrorConfirmPassword = findViewById(R.id.tvErrorConfirmPassword);
        tvErrorFullName = findViewById(R.id.tvErrorFullName);
        tvErrorPhone = findViewById(R.id.tvErrorPhone);
        tvErrorCCCD = findViewById(R.id.tvErrorCCCD);
        tvErrorLicense = findViewById(R.id.tvErrorLicense);
        tvErrorLicenseType = findViewById(R.id.tvErrorLicenseType);
        tvErrorExpiryDate = findViewById(R.id.tvErrorExpiryDate);
    }

    private boolean validateInput() {
        clearErrors();
        boolean isValid = true;
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String cccd = edtCCCD.getText().toString().trim();

        if (username.isEmpty()) { showError(edtUsername, tvErrorUsername, "Nhập Tên đăng nhập."); isValid = false; }
        if (password.isEmpty()) { showError(edtPassword, tvErrorPassword, "Nhập Mật khẩu."); isValid = false; }
        if (!password.equals(confirmPassword)) { showError(edtConfirmPassword, tvErrorConfirmPassword, "Mật khẩu không khớp."); isValid = false; }
        if (fullName.isEmpty()) { showError(edtFullName, tvErrorFullName, "Nhập Họ tên."); isValid = false; }
        if (phone.length() != 10) { showError(edtPhone, tvErrorPhone, "SĐT không hợp lệ."); isValid = false; }
        if (cccd.length() != 12) { showError(edtCCCD, tvErrorCCCD, "CCCD phải 12 số."); isValid = false; }
        return isValid;
    }

    private void checkDuplicatesAndCreate() {
        String username = edtUsername.getText().toString().trim();
        apiService.getUsers("Get").enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> uResponse) {
                if (uResponse.isSuccessful() && uResponse.body() != null) {
                    for (UserModel u : uResponse.body()) {
                        if (u.getTenDangNhap().equalsIgnoreCase(username)) {
                            showError(edtUsername, tvErrorUsername, "Tên đăng nhập đã tồn tại.");
                            return;
                        }
                    }
                    // ✅ Sửa getTaixeList -> getTaiXeList
                    apiService.getTaiXeList("Get").enqueue(new Callback<List<TaixeModel>>() {
                        @Override
                        public void onResponse(Call<List<TaixeModel>> call, Response<List<TaixeModel>> txResponse) {
                            if (txResponse.isSuccessful() && txResponse.body() != null) {
                                generateIdsAndCreate(txResponse.body(), uResponse.body());
                            }
                        }
                        @Override public void onFailure(Call<List<TaixeModel>> call, Throwable t) {}
                    });
                }
            }
            @Override public void onFailure(Call<List<UserModel>> call, Throwable t) {}
        });
    }

    private void generateIdsAndCreate(List<TaixeModel> txList, List<UserModel> uList) {
        String nextTxId = "TAI" + (txList.size() + 100);
        String nextUsId = "US" + (uList.size() + 100);
        startCreationFlow(nextTxId, nextUsId);
    }

    private void showError(EditText edt, TextView tvError, String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void clearErrors() {
        tvErrorUsername.setVisibility(View.GONE);
        tvErrorPassword.setVisibility(View.GONE);
        tvErrorConfirmPassword.setVisibility(View.GONE);
        tvErrorFullName.setVisibility(View.GONE);
        tvErrorPhone.setVisibility(View.GONE);
        tvErrorCCCD.setVisibility(View.GONE);
    }

    private void startCreationFlow(String taixeId, String userId) {
        TaixeModel taixe = new TaixeModel();
        taixe.setTaixeID(taixeId);
        taixe.setSoBangLai(edtLicense.getText().toString().trim());
        taixe.setSoCCCD(edtCCCD.getText().toString().trim());
        taixe.setLoaiBangLai(edtLicenseType.getText().toString().trim());
        taixe.setNgayHetHanBangLai("2030-01-01");
        taixe.setHinhAnhURL(selectedImageBase64);

        // ✅ Sửa createTaixe -> createTaiXe
        apiService.createTaiXe("Post", taixe).enqueue(new Callback<TaixeModel>() {
            @Override
            public void onResponse(Call<TaixeModel> call, Response<TaixeModel> response) {
                if (response.isSuccessful()) {
                    createChiTietAndAuth(taixeId, userId);
                } else {
                    Toast.makeText(CreateDriverAccountActivity.this, "Lỗi tạo tài xế", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<TaixeModel> call, Throwable t) {}
        });
    }

    private void createChiTietAndAuth(String taixeId, String userId) {
        ChiTietTaiXeModel chiTiet = new ChiTietTaiXeModel();
        chiTiet.setTaixe(taixeId);
        chiTiet.setNhaxe(currentNhaxeId);
        chiTiet.setHoTen(edtFullName.getText().toString().trim());

        apiService.createChiTietTaiXe("Post", chiTiet).enqueue(new Callback<ChiTietTaiXeModel>() {
            @Override
            public void onResponse(Call<ChiTietTaiXeModel> call, Response<ChiTietTaiXeModel> response) {
                if (response.isSuccessful()) {
                    UserModel user = new UserModel();
                    user.setUserID(userId);
                    user.setTenDangNhap(edtUsername.getText().toString().trim());
                    user.setMatKhau(edtPassword.getText().toString().trim());
                    user.setSoDienThoai(edtPhone.getText().toString().trim());
                    user.setVaitro("TaiXe");
                    user.setTaixe(taixeId);

                    apiService.createUser("Post", user).enqueue(new Callback<UserModel>() {
                        @Override
                        public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(CreateDriverAccountActivity.this, "Tạo tài khoản thành công!", Toast.LENGTH_LONG).show();
                                setResult(RESULT_OK); // ✅ Trả kết quả thành công cho DriverSelectionActivity
                                finish(); // Quay về màn hình danh sách tài xế
                            }
                        }
                        @Override public void onFailure(Call<UserModel> call, Throwable t) {}
                    });
                }
            }
            @Override public void onFailure(Call<ChiTietTaiXeModel> call, Throwable t) {}
        });
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) { return ""; }
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> edtExpiryDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }
}
