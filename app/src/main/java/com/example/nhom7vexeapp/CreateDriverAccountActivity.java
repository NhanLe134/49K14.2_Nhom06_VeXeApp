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
                    tvImageName.setText("Đã chọn: " + getFileName(uri));
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
        currentNhaxeId = pref.getString("nhaxe_id", null);
        
        initViews();

        btnClose.setOnClickListener(v -> finish());
        edtExpiryDate.setOnClickListener(v -> showDatePicker());
        btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSubmit.setOnClickListener(v -> {
            if (currentNhaxeId == null || currentNhaxeId.isEmpty()) {
                Toast.makeText(this, "Lỗi: Không tìm thấy nhà xe. Thử đăng nhập lại!", Toast.LENGTH_LONG).show();
                return;
            }
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
        String license = edtLicense.getText().toString().trim();
        String licenseType = edtLicenseType.getText().toString().trim();
        String expiryDate = edtExpiryDate.getText().toString().trim();

        if (username.isEmpty()) {
            showError(edtUsername, tvErrorUsername, "Vui lòng nhập Tên đăng nhập.");
            isValid = false;
        } else if (username.contains(" ") || !Pattern.compile("^[a-zA-Z0-9]+$").matcher(username).matches()) {
            showError(edtUsername, tvErrorUsername, "Tên đăng nhập không được chứa ký tự đặc biệt/khoảng trắng.");
            isValid = false;
        }

        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        if (password.isEmpty()) {
            showError(edtPassword, tvErrorPassword, "Vui lòng nhập Mật khẩu.");
            isValid = false;
        } else if (!Pattern.compile(passwordPattern).matcher(password).matches()) {
            showError(edtPassword, tvErrorPassword, "Mật khẩu cần tối thiểu 8 ký tự, ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt.");
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            showError(edtConfirmPassword, tvErrorConfirmPassword, "Vui lòng xác nhận mật khẩu.");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            showError(edtConfirmPassword, tvErrorConfirmPassword, "Mật khẩu xác nhận không khớp.");
            isValid = false;
        }

        if (fullName.isEmpty()) {
            showError(edtFullName, tvErrorFullName, "Vui lòng nhập Họ tên tài xế.");
            isValid = false;
        }

        if (phone.isEmpty()) {
            showError(edtPhone, tvErrorPhone, "Vui lòng nhập Số điện thoại.");
            isValid = false;
        } else if (!Pattern.compile("^\\d{10}$").matcher(phone).matches()) {
            showError(edtPhone, tvErrorPhone, "Số điện thoại phải có 10 ký tự số.");
            isValid = false;
        }

        if (cccd.isEmpty()) {
            showError(edtCCCD, tvErrorCCCD, "Vui lòng nhập Số CCCD.");
            isValid = false;
        } else if (!Pattern.compile("^\\d{12}$").matcher(cccd).matches()) {
            showError(edtCCCD, tvErrorCCCD, "Số CCCD phải có 12 ký tự số.");
            isValid = false;
        }

        if (license.isEmpty()) {
            showError(edtLicense, tvErrorLicense, "Vui lòng nhập Số bằng lái.");
            isValid = false;
        }

        if (licenseType.isEmpty()) {
            showError(edtLicenseType, tvErrorLicenseType, "Vui lòng nhập Loại bằng lái.");
            isValid = false;
        }

        if (expiryDate.isEmpty()) {
            showError(edtExpiryDate, tvErrorExpiryDate, "Vui lòng nhập Ngày hết hạn bằng lái.");
            isValid = false;
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(expiryDate);
                if (date != null && date.before(new Date())) {
                    showError(edtExpiryDate, tvErrorExpiryDate, "Ngày hết hạn bằng lái phải lớn hơn hiện tại.");
                    isValid = false;
                }
            } catch (Exception e) {
                isValid = false;
            }
        }

        return isValid;
    }

    private void checkDuplicatesAndCreate() {
        String username = edtUsername.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String cccd = edtCCCD.getText().toString().trim();

        apiService.getUsers("Get").enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> uResponse) {
                if (uResponse.isSuccessful() && uResponse.body() != null) {
                    for (UserModel u : uResponse.body()) {
                        if (u.getTenDangNhap().equalsIgnoreCase(username)) {
                            showError(edtUsername, tvErrorUsername, "Tên đăng nhập này đã có người sử dụng.");
                            return;
                        }
                        if (u.getSoDienThoai().equals(phone)) {
                            showError(edtPhone, tvErrorPhone, "Số điện thoại này đã được đăng ký.");
                            return;
                        }
                    }
                    
                    apiService.getTaixeList("Get").enqueue(new Callback<List<TaixeModel>>() {
                        @Override
                        public void onResponse(Call<List<TaixeModel>> call, Response<List<TaixeModel>> txResponse) {
                            if (txResponse.isSuccessful() && txResponse.body() != null) {
                                for (TaixeModel tx : txResponse.body()) {
                                    if (tx.getSoCCCD() != null && tx.getSoCCCD().equals(cccd)) {
                                        showError(edtCCCD, tvErrorCCCD, "Số CCCD này đã tồn tại trên hệ thống.");
                                        return;
                                    }
                                }
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
        int maxTx = 0;
        for (TaixeModel tx : txList) {
            try {
                String numStr = tx.getTaixeID().replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    int num = Integer.parseInt(numStr);
                    if (num > maxTx) maxTx = num;
                }
            } catch (Exception e) {}
        }

        int maxUs = 0;
        for (UserModel u : uList) {
            try {
                String numStr = u.getUserID().replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    int num = Integer.parseInt(numStr);
                    if (num > maxUs) maxUs = num;
                }
            } catch (Exception e) {}
        }

        String nextTxId = String.format(Locale.getDefault(), "TAI%05d", maxTx + 1);
        String nextUsId = String.format(Locale.getDefault(), "US%05d", maxUs + 1);
        startCreationFlow(nextTxId, nextUsId);
    }

    private void showError(EditText edt, TextView tvError, String message) {
        edt.setBackgroundResource(R.drawable.bg_input_error);
        tvError.setText(message);
        tvError.setTypeface(null, Typeface.ITALIC);
        tvError.setVisibility(View.VISIBLE);
    }

    private void clearErrors() {
        int[] edts = {R.id.edtUsername, R.id.edtPassword, R.id.edtConfirmPassword, R.id.edtFullName, R.id.edtPhone, R.id.edtCCCD, R.id.edtLicense, R.id.edtLicenseType, R.id.edtExpiryDate};
        int[] tvs = {R.id.tvErrorUsername, R.id.tvErrorPassword, R.id.tvErrorConfirmPassword, R.id.tvErrorFullName, R.id.tvErrorPhone, R.id.tvErrorCCCD, R.id.tvErrorLicense, R.id.tvErrorLicenseType, R.id.tvErrorExpiryDate};
        
        for (int id : edts) {
            View v = findViewById(id);
            if (v != null) v.setBackgroundResource(R.drawable.bg_input_white);
        }
        for (int id : tvs) {
            View v = findViewById(id);
            if (v != null) v.setVisibility(View.GONE);
        }
    }

    private void startCreationFlow(String taixeId, String userId) {
        TaixeModel taixe = new TaixeModel();
        taixe.setTaixeID(taixeId);
        taixe.setSoBangLai(edtLicense.getText().toString().trim());
        taixe.setSoCCCD(edtCCCD.getText().toString().trim());
        taixe.setLoaiBangLai(edtLicenseType.getText().toString().trim());
        taixe.setHinhAnhURL(selectedImageBase64.isEmpty() ? null : selectedImageBase64);
        
        try {
            SimpleDateFormat fromUser = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat toApi = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            taixe.setNgayHetHanBangLai(toApi.format(fromUser.parse(edtExpiryDate.getText().toString())));
        } catch (Exception e) {
            taixe.setNgayHetHanBangLai("2030-01-01");
        }

        apiService.createTaixe("Post", taixe).enqueue(new Callback<TaixeModel>() {
            @Override
            public void onResponse(Call<TaixeModel> call, Response<TaixeModel> response) {
                if (response.isSuccessful()) {
                    createChiTietAndAuth(taixeId, userId);
                } else {
                    handleApiError("BƯỚC 1: Lỗi tạo Taixe", response);
                }
            }
            @Override public void onFailure(Call<TaixeModel> call, Throwable t) {
                Toast.makeText(CreateDriverAccountActivity.this, "Lỗi kết nối Bước 1", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createChiTietAndAuth(String taixeId, String userId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String ngayBatDau = sdf.format(cal.getTime());
        cal.add(Calendar.YEAR, 1);
        String ngayKetThuc = sdf.format(cal.getTime());

        ChiTietTaiXeModel chiTiet = new ChiTietTaiXeModel();
        chiTiet.setTaixe(taixeId);
        chiTiet.setNhaxe(currentNhaxeId);
        chiTiet.setHoTen(edtFullName.getText().toString().trim());
        chiTiet.setTennhaxe("Nhà xe Quản lý");
        chiTiet.setNgayBatDau(ngayBatDau);
        chiTiet.setNgayKetThuc(ngayKetThuc);

        apiService.createChiTietTaiXe("Post", chiTiet).enqueue(new Callback<ChiTietTaiXeModel>() {
            @Override
            public void onResponse(Call<ChiTietTaiXeModel> call, Response<ChiTietTaiXeModel> response) {
                if (response.isSuccessful()) {
                    UserModel user = new UserModel();
                    user.setUserID(userId);
                    user.setTenDangNhap(edtUsername.getText().toString().trim());
                    user.setMatKhau(edtPassword.getText().toString().trim());
                    user.setSoDienThoai(edtPhone.getText().toString().trim());
                    user.setVaitro("taixe");
                    user.setTaixe(taixeId);
                    user.setNhaxe(currentNhaxeId);

                    apiService.createUser("Post", user).enqueue(new Callback<UserModel>() {
                        @Override
                        public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                            if (response.isSuccessful()) {
                                showSuccessDialog();
                            } else {
                                handleApiError("BƯỚC 3: Lỗi tạo User", response);
                            }
                        }
                        @Override public void onFailure(Call<UserModel> call, Throwable t) {
                            Toast.makeText(CreateDriverAccountActivity.this, "Lỗi kết nối Bước 3", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    handleApiError("BƯỚC 2: Lỗi tạo Chi tiết", response);
                }
            }
            @Override public void onFailure(Call<ChiTietTaiXeModel> call, Throwable t) {
                Toast.makeText(CreateDriverAccountActivity.this, "Lỗi kết nối Bước 2", Toast.LENGTH_SHORT).show();
            }
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
        } catch (Exception e) {
            return "";
        }
    }

    private String getFileName(Uri uri) {
        String path = uri.getPath();
        if (path == null) return "image.jpg";
        int cut = path.lastIndexOf('/');
        if (cut != -1) path = path.substring(cut + 1);
        return path;
    }

    private void showSuccessDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_driver_success, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(() -> { if (!isFinishing()) { dialog.dismiss(); finish(); } }, 2000);
    }

    private void handleApiError(String tag, Response<?> response) {
        String detail = "Unknown";
        try { detail = response.errorBody().string(); } catch (Exception e) {}
        Log.e("API_ERROR", tag + ": " + detail);
        Toast.makeText(this, tag + " (Code " + response.code() + ")", Toast.LENGTH_LONG).show();
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> edtExpiryDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
