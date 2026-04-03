package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.nhom7vexeapp.api.LoginRequest;
import com.example.nhom7vexeapp.api.LoginResponse;

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
        // Đảm bảo sử dụng ApiService từ package .api
        apiService = ApiClient.getClient().create(ApiService.class);

        tvSwitchMode.setOnClickListener(v -> {
            isOperatorMode = !isOperatorMode;
            updateUI();
        });

        tvRegisterCustomer.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerRegisterActivity.class));
        });

        tvRegisterOperator.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnLoginCustomer.setOnClickListener(v -> handleCustomerLogin());
        btnLoginOperator.setOnClickListener(v -> handleOperatorLogin());
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
            tvLoginTitle.setText("Đăng nhập");
            layoutCustomerLogin.setVisibility(View.GONE);
            layoutOperatorLogin.setVisibility(View.VISIBLE);
            tvSwitchMode.setText("Bạn là khách hàng?");
            tvSwitchMode.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            tvLoginTitle.setText("Đăng nhập");
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

        Toast.makeText(this, "Đang kết nối server...", Toast.LENGTH_SHORT).show();
        
        apiService.checkUserRole(phoneInput).enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CustomerResponse> users = response.body();
                    CustomerResponse foundUser = null;

                    // Tìm chính xác user theo SĐT trong danh sách trả về
                    for (CustomerResponse u : users) {
                        if (u.getSdt() != null && u.getSdt().equals(phoneInput)) {
                            foundUser = u;
                            break;
                        }
                    }

                    if (foundUser != null) {
                        String role = foundUser.getVaitro();
                        Log.d("LOGIN_DEBUG", "Phone: " + phoneInput + " | Role từ server: " + role);

                        if (role != null && role.equalsIgnoreCase("KhachHang")) {
                            proceedCustomerLogin(foundUser.getSdt(), foundUser.getTenKhachHang(), foundUser.getNgaySinh());
                        } else {
                            Toast.makeText(LoginActivity.this, "Đây là tài khoản Nhà xe (" + role + "). Hãy đăng nhập bên mục Nhà xe!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Số điện thoại " + phoneInput + " chưa đăng ký!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CustomerResponse>> call, Throwable t) {
                // Hiển thị lỗi thật sự để debug (ví dụ: Connection Refused, Timeout...)
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("LOGIN_DEBUG", "Connection error", t);
            }
        });
    }

    private void proceedCustomerLogin(String phone, String name, String dob) {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("customerPhone", phone);
        editor.putString("customerName", name);
        editor.putString("customerDob", dob);
        editor.apply();

        Toast.makeText(this, "Chào mừng " + name, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void handleOperatorLogin() {
        String user = edtUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tài khoản/mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.login(new LoginRequest(user, pass)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("token", response.body().getToken());
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("role", "operator");
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Đăng nhập nhà xe thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, OperatorMainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai thông tin nhà xe!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
