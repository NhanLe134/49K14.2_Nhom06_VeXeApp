package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.UserModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private boolean isOperatorMode = false;
    private LinearLayout layoutCustomerLogin, layoutOperatorLogin;
    private TextView tvLoginTitle, tvSwitchMode, tvRegisterCustomer, tvRegisterOperator;
    private EditText edtPhoneLogin, edtUsername, edtPassword;
    private Button btnLoginCustomer, btnLoginOperator;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        apiService = ApiClient.getClient().create(ApiService.class);

        // Chuyển đổi chế độ đăng nhập
        if (tvSwitchMode != null) {
            tvSwitchMode.setOnClickListener(v -> {
                isOperatorMode = !isOperatorMode;
                updateUI();
            });
        }

        // Đăng ký và Đăng nhập cho Khách hàng
        if (tvRegisterCustomer != null) {
            tvRegisterCustomer.setOnClickListener(v -> startActivity(new Intent(this, CustomerRegisterActivity.class)));
        }
        if (btnLoginCustomer != null) {
            btnLoginCustomer.setOnClickListener(v -> handleCustomerLogin());
        }

        // Đăng ký và Đăng nhập cho Nhà xe
        if (tvRegisterOperator != null) {
            tvRegisterOperator.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        }
        if (btnLoginOperator != null) {
            btnLoginOperator.setOnClickListener(v -> handleOperatorLogin());
        }
    }

    private void initViews() {
        layoutCustomerLogin = findViewById(R.id.layoutCustomerLogin);
        layoutOperatorLogin = findViewById(R.id.layoutOperatorLogin);
        tvLoginTitle = findViewById(R.id.tvLoginTitle);
        tvSwitchMode = findViewById(R.id.tvSwitchMode);
        tvRegisterCustomer = findViewById(R.id.tvRegisterCustomer);
        tvRegisterOperator = findViewById(R.id.tvRegisterOperator);
        edtPhoneLogin = findViewById(R.id.edtPhoneLogin);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLoginCustomer = findViewById(R.id.btnLoginCustomer);
        btnLoginOperator = findViewById(R.id.btnLoginOperator);
    }

    private void updateUI() {
        if (isOperatorMode) {
            tvLoginTitle.setText("Đăng nhập Nhà xe");
            layoutCustomerLogin.setVisibility(View.GONE);
            layoutOperatorLogin.setVisibility(View.VISIBLE);
            tvSwitchMode.setText("Bạn là khách hàng?");
            tvSwitchMode.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            tvLoginTitle.setText("Đăng nhập Khách hàng");
            layoutCustomerLogin.setVisibility(View.VISIBLE);
            layoutOperatorLogin.setVisibility(View.GONE);
            tvSwitchMode.setText("Bạn là nhà xe?");
            tvSwitchMode.setTextColor(android.graphics.Color.parseColor("#FF5722"));
        }
    }

    private void handleCustomerLogin() {
        final String phoneInput = edtPhoneLogin.getText().toString().trim();
        if (phoneInput.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getUsers("Get").enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel found = null;
                    for (UserModel u : response.body()) {
                        if (phoneInput.equals(u.getSoDienThoai())) {
                            found = u;
                            break;
                        }
                    }

                    if (found != null) {
                        // Lấy ID khách hàng thực tế (KhachHangID) hoặc fallback về UserID
                        String realKhId = found.getKhachHang();
                        if (realKhId == null || realKhId.isEmpty()) realKhId = found.getUserID();

                        saveAndGo(found, "customer", phoneInput, realKhId);
                    } else {
                        Toast.makeText(LoginActivity.this, "Số điện thoại chưa đăng ký!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleOperatorLogin() {
        final String user = edtUsername.getText().toString().trim();
        final String pass = edtPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getUsers("Get").enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel found = null;
                    for (UserModel u : response.body()) {
                        if (user.equals(u.getTenDangNhap()) && pass.equals(u.getMatKhau())) {
                            found = u;
                            break;
                        }
                    }

                    if (found != null && "Nhaxe".equalsIgnoreCase(found.getVaitro())) {
                        String realOpId = found.getNhaxe();
                        saveAndGo(found, "operator", user, realOpId);
                    } else {
                        Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAndGo(UserModel userModel, String role, String loginIdentity, String targetId) {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", role);
        editor.putString("user_id", userModel.getUserID()); // ID bảng NguoiDung chung

        if ("operator".equals(role)) {
            // Ưu tiên NhaXeID, nếu null lấy UserID
            String finalOpUid = (targetId != null && !targetId.isEmpty()) ? targetId : userModel.getUserID();
            editor.putString("op_uid", finalOpUid);
            editor.putString("op_user", loginIdentity);

            startActivity(new Intent(this, OperatorMainActivity.class));
        } else {
            // Thông tin cho Khách hàng
            editor.putString("customerUid", targetId); // ID bảng KhachHang
            editor.putString("customerPhone", loginIdentity);
            editor.putString("customerName", userModel.getHovaten());

            startActivity(new Intent(this, MainActivity.class));
        }

        editor.apply();
        finish();
    }
}