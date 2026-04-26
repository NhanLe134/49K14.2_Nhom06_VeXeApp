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
import com.example.nhom7vexeapp.api.CustomerResponse;
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

        if (tvSwitchMode != null) {
            tvSwitchMode.setOnClickListener(v -> {
                isOperatorMode = !isOperatorMode;
                updateUI();
            });
        }

        if (tvRegisterCustomer != null) {
            tvRegisterCustomer.setOnClickListener(v -> startActivity(new Intent(this, CustomerRegisterActivity.class)));
        }
        if (tvRegisterOperator != null) {
            tvRegisterOperator.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        }

        if (btnLoginCustomer != null) {
            btnLoginCustomer.setOnClickListener(v -> handleCustomerLogin());
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
        if (tvLoginTitle != null) tvLoginTitle.setText(isOperatorMode ? "Đăng nhập Nhà xe" : "Đăng nhập Khách hàng");
        if (layoutCustomerLogin != null) layoutCustomerLogin.setVisibility(isOperatorMode ? View.GONE : View.VISIBLE);
        if (layoutOperatorLogin != null) layoutOperatorLogin.setVisibility(isOperatorMode ? View.VISIBLE : View.GONE);

        if (tvSwitchMode != null) {
            tvSwitchMode.setText(isOperatorMode ? "Bạn là khách hàng?" : "Bạn là nhà xe?");
            if (isOperatorMode) {
                tvSwitchMode.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else {
                tvSwitchMode.setTextColor(android.graphics.Color.parseColor("#FF5722"));
            }
        }
    }

    private void handleCustomerLogin() {
        final String phoneInput = edtPhoneLogin.getText().toString().trim();
        if (phoneInput.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ĐÃ SỬA: Xóa tham số "Get" để khớp với ApiService
        apiService.getUsers().enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerResponse foundUser = null;
                    for (CustomerResponse u : response.body()) {
                        if (phoneInput.equals(u.getSdt())) {
                            foundUser = u; break;
                        }
                    }

                    if (foundUser != null) {
                        String realKhId = foundUser.getKhachHang();
                        if (realKhId == null || realKhId.isEmpty()) realKhId = foundUser.getUserID();
                        saveAndGo(foundUser, "customer", realKhId);
                    } else {
                        Toast.makeText(LoginActivity.this, "Số điện thoại chưa đăng ký!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<CustomerResponse>> call, Throwable t) {
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

        // ĐÃ SỬA: Xóa tham số "Get" để khớp với ApiService
        apiService.getUsers().enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerResponse foundOp = null;
                    for (CustomerResponse u : response.body()) {
                        if (user.equals(u.getTenKhachHang()) && pass.equals(u.getMatKhau())) {
                            foundOp = u; break;
                        }
                    }

                    if (foundOp != null && "Nhaxe".equalsIgnoreCase(foundOp.getVaitro())) {
                        String realOpId = foundOp.getNhaxe();
                        if (realOpId == null || realOpId.isEmpty()) realOpId = foundOp.getUserID();
                        saveAndGo(foundOp, "operator", realOpId);
                    } else {
                        Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<CustomerResponse>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAndGo(CustomerResponse user, String role, String targetId) {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", role);

        if ("operator".equals(role)) {
            editor.putString("op_uid", targetId);
            editor.putString("op_user", user.getTenKhachHang());
            startActivity(new Intent(this, OperatorMainActivity.class));
        } else {
            // ĐỒNG BỘ CÁC KEY QUAN TRỌNG CHO CHỨC NĂNG ĐẶT VÉ CỦA BẠN
            editor.putString("customerUid", targetId);
            editor.putString("user_id", user.getUserID());
            editor.putString("customerName", user.getTenKhachHang());
            editor.putString("customerPhone", user.getSdt());
            startActivity(new Intent(this, MainActivity.class));
        }
        editor.apply();
        finish();
    }
}
