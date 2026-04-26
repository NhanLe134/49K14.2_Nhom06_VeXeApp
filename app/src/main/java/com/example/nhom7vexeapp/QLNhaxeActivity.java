package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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

public class QLNhaxeActivity extends AppCompatActivity {

    private LinearLayout layoutViewMode, layoutEditMode;
    private TextView txtToolbarTitle, txtFileName;
    private TextView tvViewBusName, tvViewBusNameHeader, tvViewRepName, tvViewAddress, tvViewPhone, tvViewEmail;
    private EditText edtBusName, edtRepName, edtAddress, edtPhone;
    private TextView tvErrorBusName, tvErrorRepName, tvErrorAddress, tvErrorPhone;
    private Button btnEdit, btnSave, btnCancel;
    private TextView btnChooseFile;
    private ImageView btnBack;
    private ImageView imgLogo, imgViewBanner, imgEditPreview;

    private boolean isEditing = false;
    private String opUid, opEmail = "";
    private String selectedImageBase64 = "";
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        imgEditPreview.setImageURI(uri);
                        selectedImageBase64 = encodeImageToBase64(uri);
                        txtFileName.setText("Đã chọn ảnh mới");
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
        // Tab Trang chủ
        View navHome = findViewById(R.id.nav_home_op_main);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, OperatorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        // Tab Tài xế (Hiện tại)
        View navDriver = findViewById(R.id.nav_driver_op);
        if (navDriver != null) {
            navDriver.setOnClickListener(v -> {
                // Đang ở trang này
            });
        }

        // Tab Phương tiện
        View navVehicle = findViewById(R.id.nav_vehicle_op);
        if (navVehicle != null) {
            navVehicle.setOnClickListener(v -> {
                startActivity(new Intent(this, PhuongTienManagementActivity.class));
            });
        }

        // Tab Chuyến xe
        View navTrip = findViewById(R.id.nav_trip_op);
        if (navTrip != null) {
            navTrip.setOnClickListener(v -> {
                startActivity(new Intent(this, TripListActivity.class));
            });
        }

        // Tab Tuyến xe
        View navRoute = findViewById(R.id.nav_route_op);
        if (navRoute != null) {
            navRoute.setOnClickListener(v -> {
                startActivity(new Intent(this, QLTuyenxeActivity.class));
            });
        }
    }

    private void loadDataFromApi() {
        if (opUid == null || opUid.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID nhà xe!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getNhaXeDetail(opUid).enqueue(new Callback<NhaXe>() {
            @Override
            public void onResponse(Call<NhaXe> call, Response<NhaXe> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NhaXe nhaXe = response.body();
                    updateUI(nhaXe);
                } else {
                    Toast.makeText(QLNhaxeActivity.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NhaXe> call, Throwable t) {
                Toast.makeText(QLNhaxeActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(NhaXe nhaXe) {
        String name = nhaXe.getBusName() != null ? nhaXe.getBusName() : "Chưa cập nhật";
        String rep = nhaXe.getRepresentative() != null ? nhaXe.getRepresentative() : "Chưa cập nhật";
        String address = nhaXe.getAddress() != null ? nhaXe.getAddress() : "Chưa cập nhật";
        String phone = nhaXe.getPhone() != null ? nhaXe.getPhone() : "Chưa cập nhật";
        String email = nhaXe.getEmail() != null ? nhaXe.getEmail() : "Chưa cập nhật";
        opEmail = email;

        if (tvViewBusName != null) tvViewBusName.setText(name);
        if (tvViewBusNameHeader != null) tvViewBusNameHeader.setText(name);
        if (tvViewRepName != null) tvViewRepName.setText(rep);
        if (tvViewAddress != null) tvViewAddress.setText(address);
        if (tvViewPhone != null) tvViewPhone.setText(phone);
        if (tvViewEmail != null) tvViewEmail.setText(email);

        if (edtBusName != null) edtBusName.setText(name);
        if (edtRepName != null) edtRepName.setText(rep);
        if (edtAddress != null) edtAddress.setText(address);
        if (edtPhone != null) edtPhone.setText(phone);

        if (imgViewBanner != null && nhaXe.getBannerUrl() != null && !nhaXe.getBannerUrl().isEmpty()) {
            selectedImageBase64 = nhaXe.getBannerUrl();
            Glide.with(this)
                .load(nhaXe.getBannerUrl())
                .placeholder(R.drawable.banner_nhaxe)
                .into(imgViewBanner);
            
            if (imgEditPreview != null) {
                Glide.with(this).load(nhaXe.getBannerUrl()).into(imgEditPreview);
            }
        }
    }


    private void enterEditMode() {
        isEditing = true;
        
        // Đồng bộ dữ liệu hiện có từ TextView sang EditText để đảm bảo form không bị trống
        syncDataToEditForm();
        
        if (layoutViewMode != null) layoutViewMode.setVisibility(View.GONE);
        if (layoutEditMode != null) layoutEditMode.setVisibility(View.VISIBLE);
        if (txtToolbarTitle != null) txtToolbarTitle.setText("Chỉnh sửa Thông tin nhà xe");
        clearErrors();
    }

    private void syncDataToEditForm() {
        if (tvViewBusName != null && edtBusName != null) {
            String val = tvViewBusName.getText().toString();
            edtBusName.setText(val.equals("Chưa cập nhật") ? "" : val);
        }
        if (tvViewRepName != null && edtRepName != null) {
            String val = tvViewRepName.getText().toString();
            edtRepName.setText(val.equals("Chưa cập nhật") ? "" : val);
        }
        if (tvViewAddress != null && edtAddress != null) {
            String val = tvViewAddress.getText().toString();
            edtAddress.setText(val.equals("Chưa cập nhật") ? "" : val);
        }
        if (tvViewPhone != null && edtPhone != null) {
            String val = tvViewPhone.getText().toString();
            edtPhone.setText(val.equals("Chưa cập nhật") ? "" : val);
        }
    }

    private void exitEditMode() {
        isEditing = false;
        if (layoutViewMode != null) layoutViewMode.setVisibility(View.VISIBLE);
        if (layoutEditMode != null) layoutEditMode.setVisibility(View.GONE);
        if (txtToolbarTitle != null) txtToolbarTitle.setText("Thông tin nhà xe");
    }

    private void showCancelConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_cancel, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnNo = dialogView.findViewById(R.id.btnNo);
        Button btnYes = dialogView.findViewById(R.id.btnYes);

        if (btnNo != null) btnNo.setOnClickListener(v -> dialog.dismiss());
        if (btnYes != null) btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            exitEditMode();
        });

        dialog.show();
    }

    private void validateAndSave() {
        clearErrors();
        boolean isValid = true;

        String busName = edtBusName.getText().toString().trim();
        String repName = edtRepName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(busName)) {
            showFieldError(edtBusName, tvErrorBusName, "Vui lòng nhập Tên nhà xe.");
            isValid = false;
        }
        if (TextUtils.isEmpty(repName)) {
            showFieldError(edtRepName, tvErrorRepName, "Vui lòng nhập Họ tên người đại diện.");
            isValid = false;
        }
        if (TextUtils.isEmpty(address)) {
            showFieldError(edtAddress, tvErrorAddress, "Vui lòng nhập Địa chỉ trụ sở.");
            isValid = false;
        }
        if (TextUtils.isEmpty(phone)) {
            showFieldError(edtPhone, tvErrorPhone, "Vui lòng nhập Số điện thoại.");
            isValid = false;
        }

        if (!isValid) return;

        if (isValid) {
            handleUpdate(busName, repName, address, phone);
        }
    }

    private void handleUpdate(String name, String rep, String addr, String phone) {
        Map<String, String> data = new HashMap<>();
        data.put("NhaxeID", opUid);
        data.put("Tennhaxe", name);
        data.put("TenNguoiDaiDien", rep);
        data.put("DiaChiTruSo", addr);
        data.put("SoDienThoai", phone);
        data.put("Email", opEmail.isEmpty() ? "nhaxe@gmail.com" : opEmail);
        data.put("AnhDaiDienURL", selectedImageBase64);

        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Đang lưu thông tin nhà xe...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        apiService.updateNhaXeProfile(opUid, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    updateViewMode(name, rep, addr, phone);
                    showSuccessPopup();
                } else {
                    String errorMsg = "Lưu thất bại! (Mã lỗi: " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\nServer phản hồi: " + response.errorBody().string();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    
                    new AlertDialog.Builder(QLNhaxeActivity.this)
                            .setTitle("Lỗi Backend")
                            .setMessage(errorMsg)
                            .setPositiveButton("Đã hiểu", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(QLNhaxeActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            return "";
        }
    }

    private void showFieldError(EditText editText, TextView errorTextView, String message) {
        editText.setBackgroundResource(R.drawable.bg_input_error);
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    private void clearErrors() {
        if (edtBusName != null) edtBusName.setBackgroundResource(R.drawable.bg_input_white);
        if (edtRepName != null) edtRepName.setBackgroundResource(R.drawable.bg_input_white);
        if (edtAddress != null) edtAddress.setBackgroundResource(R.drawable.bg_input_white);
        if (edtPhone != null) edtPhone.setBackgroundResource(R.drawable.bg_input_white);

        if (tvErrorBusName != null) tvErrorBusName.setVisibility(View.GONE);
        if (tvErrorRepName != null) tvErrorRepName.setVisibility(View.GONE);
        if (tvErrorAddress != null) tvErrorAddress.setVisibility(View.GONE);
        if (tvErrorPhone != null) tvErrorPhone.setVisibility(View.GONE);
    }

    private void showSuccessPopup() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_success, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                exitEditMode();
            }
        }, 2000);
    }

    private void updateViewMode(String name, String rep, String addr, String phone) {
        if (tvViewBusName != null) tvViewBusName.setText(name);
        if (tvViewBusNameHeader != null) tvViewBusNameHeader.setText(name);
        if (tvViewRepName != null) tvViewRepName.setText(rep);
        if (tvViewAddress != null) tvViewAddress.setText(addr);
        if (tvViewPhone != null) tvViewPhone.setText(phone);
    }
}
