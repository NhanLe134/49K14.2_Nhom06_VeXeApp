package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import android.util.Base64;
import android.graphics.Bitmap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.NhaXe;

import android.content.SharedPreferences;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// #Trang
public class QLNhaxeActivity extends AppCompatActivity {

    private static final String TAG = "QLNhaxeActivity";
    private LinearLayout layoutViewMode, layoutEditMode;
    private TextView txtToolbarTitle, txtFileName;
    private TextView tvViewBusName, tvViewBusNameHeader, tvViewRepName, tvViewAddress, tvViewPhone, tvViewEmail;
    private EditText edtBusName, edtRepName, edtAddress, edtPhone, edtEmail;
    private TextView tvErrorBusName, tvErrorRepName, tvErrorAddress, tvErrorPhone;
    private Button btnEdit, btnSave, btnCancel;
    private TextView btnChooseFile;
    private ImageView btnBack;
    private ImageView imgViewBanner, imgEditPreview;

    private boolean isEditing = false;
    private String opUid;
    private String selectedImageBase64 = "";
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imgEditPreview.setImageBitmap(bitmap);
                            selectedImageBase64 = encodeImageToBase64(uri);
                            txtFileName.setText("Đã chọn ảnh mới");
                        } catch (Exception e) {
                            Log.e(TAG, "Error picking image", e);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ql_nhaxe);

        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        opUid = pref.getString("op_uid", "");
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupEvents();
        setupBottomNavigation();
        loadDataFromApi();
    }


    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
        layoutViewMode = findViewById(R.id.layoutViewMode);
        layoutEditMode = findViewById(R.id.layoutEditMode);
        tvViewBusName = findViewById(R.id.tvViewBusName);
        tvViewBusNameHeader = findViewById(R.id.tvViewBusNameHeader);
        tvViewRepName = findViewById(R.id.tvViewRepName);
        tvViewAddress = findViewById(R.id.tvViewAddress);
        tvViewPhone = findViewById(R.id.tvViewPhone);
        tvViewEmail = findViewById(R.id.tvViewEmail);
        btnEdit = findViewById(R.id.btnEdit);
        imgViewBanner = findViewById(R.id.imgViewBanner);
        
        edtBusName = findViewById(R.id.edtBusName);
        edtRepName = findViewById(R.id.edtRepName);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);

        tvErrorBusName = findViewById(R.id.tvErrorBusName);
        tvErrorRepName = findViewById(R.id.tvErrorRepName);
        tvErrorAddress = findViewById(R.id.tvErrorAddress);
        tvErrorPhone = findViewById(R.id.tvErrorPhone);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        txtFileName = findViewById(R.id.txtFileName);
        imgEditPreview = findViewById(R.id.imgEditPreview);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> {
            if (isEditing) {
                showCancelConfirmationDialog();
            } else {
                finish();
            }
        });

        btnEdit.setOnClickListener(v -> enterEditMode());
        btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());
        btnSave.setOnClickListener(v -> validateAndSave());
        btnChooseFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });
    }

    private void setupBottomNavigation() {
        View home = findViewById(R.id.nav_home_op_main);
        if (home != null) home.setOnClickListener(v -> {
            startActivity(new Intent(this, OperatorMainActivity.class));
            finish();
        });
        
        View navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) navRoute.setOnClickListener(v -> {
            startActivity(new Intent(this, QLTuyenxeActivity.class));
            finish();
        });
    }

    private void loadDataFromApi() {
        if (opUid == null || opUid.isEmpty()) return;

        apiService.getNhaXeDetail(opUid).enqueue(new Callback<NhaXe>() {
            @Override
            public void onResponse(Call<NhaXe> call, Response<NhaXe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                }
            }
            @Override public void onFailure(Call<NhaXe> call, Throwable t) {
                Log.e(TAG, "Load API Failure", t);
            }
        });
    }

    private void updateUI(NhaXe nhaXe) {
        String name = nhaXe.getBusName() != null ? nhaXe.getBusName() : "";
        String rep = nhaXe.getRepresentative() != null ? nhaXe.getRepresentative() : "";
        String addr = nhaXe.getAddress() != null ? nhaXe.getAddress() : "";
        String phone = nhaXe.getPhone() != null ? nhaXe.getPhone() : "";
        String email = nhaXe.getEmail() != null ? nhaXe.getEmail() : "";

        tvViewBusName.setText(name);
        tvViewBusNameHeader.setText(name);
        tvViewRepName.setText(rep);
        tvViewAddress.setText(addr);
        tvViewPhone.setText(phone);
        tvViewEmail.setText(email);

        edtBusName.setText(name);
        edtRepName.setText(rep);
        edtAddress.setText(addr);
        edtPhone.setText(phone);
        if (edtEmail != null) edtEmail.setText(email);

        if (nhaXe.getBannerUrl() != null && !nhaXe.getBannerUrl().isEmpty()) {
            selectedImageBase64 = nhaXe.getBannerUrl();
            Glide.with(this).load(nhaXe.getBannerUrl()).placeholder(R.drawable.banner_nhaxe).into(imgViewBanner);
            Glide.with(this).load(nhaXe.getBannerUrl()).into(imgEditPreview);
        }
    }

    private void enterEditMode() {
        isEditing = true;
        layoutViewMode.setVisibility(View.GONE);
        layoutEditMode.setVisibility(View.VISIBLE);
        txtToolbarTitle.setText("Chỉnh sửa Thông tin nhà xe");
        clearErrors();
    }

    private void exitEditMode() {
        isEditing = false;
        layoutViewMode.setVisibility(View.VISIBLE);
        layoutEditMode.setVisibility(View.GONE);
        txtToolbarTitle.setText("Thông tin nhà xe");
    }

    private void showCancelConfirmationDialog() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_cancel, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        dv.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());
        dv.findViewById(R.id.btnYes).setOnClickListener(v -> {
            dialog.dismiss();
            exitEditMode();
        });
        dialog.show();
    }

    private void validateAndSave() {
        clearErrors();
        String busName = edtBusName.getText().toString().trim();
        String repName = edtRepName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail != null ? edtEmail.getText().toString().trim() : tvViewEmail.getText().toString();

        if (busName.isEmpty()) { showFieldError(edtBusName, tvErrorBusName, "Vui lòng nhập tên nhà xe"); return; }
        
        handleUpdate(busName, repName, address, phone, email);
    }

    private void handleUpdate(String name, String rep, String addr, String phone, String email) {
        Map<String, String> data = new HashMap<>();
        data.put("NhaxeID", opUid);
        data.put("Tennhaxe", name);
        data.put("TenNguoiDaiDien", rep);
        data.put("DiaChiTruSo", addr);
        data.put("SoDienThoai", phone);
        data.put("Email", email);
        data.put("AnhDaiDien", selectedImageBase64);

        apiService.updateNhaXeProfile(opUid, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showSuccessPopup();
                } else {
                    Toast.makeText(QLNhaxeActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(QLNhaxeActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessPopup() {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_update_success, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dv).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                exitEditMode();
                loadDataFromApi();
            }
        }, 2000);
    }

    private String encodeImageToBase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
            return "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) { return ""; }
    }

    private void showFieldError(EditText et, TextView tv, String msg) {
        et.setBackgroundResource(R.drawable.bg_input_error);
        if (tv != null) { tv.setText(msg); tv.setVisibility(View.VISIBLE); }
    }

    private void clearErrors() {
        edtBusName.setBackgroundResource(R.drawable.bg_input_white);
        edtRepName.setBackgroundResource(R.drawable.bg_input_white);
        edtAddress.setBackgroundResource(R.drawable.bg_input_white);
        edtPhone.setBackgroundResource(R.drawable.bg_input_white);
        if (tvErrorBusName != null) tvErrorBusName.setVisibility(View.GONE);
        if (tvErrorRepName != null) tvErrorRepName.setVisibility(View.GONE);
        if (tvErrorAddress != null) tvErrorAddress.setVisibility(View.GONE);
        if (tvErrorPhone != null) tvErrorPhone.setVisibility(View.GONE);
    }
}
