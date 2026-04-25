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

        tvSwitchMode.setOnClickListener(v -> {
            isOperatorMode = !isOperatorMode;
            updateUI();
        });

        tvRegisterCustomer.setOnClickListener(v -> startActivity(new Intent(this, CustomerRegisterActivity.class)));
        tvRegisterOperator.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

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

        apiService.getUsers().enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerResponse foundUser = null;
                    for (CustomerResponse u : response.body()) {
                        if (u.getSdt() != null && u.getSdt().equals(phoneInput)) {
                            foundUser = u; break;
                        }
                    }

                    if (foundUser != null && "KhachHang".equalsIgnoreCase(foundUser.getVaitro())) {
                        saveAndGo(foundUser.getUserID(), "customer");
                    } else {
                        Toast.makeText(LoginActivity.this, "SĐT chưa đăng ký hoặc không phải khách hàng!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<CustomerResponse>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

        apiService.getUsers().enqueue(new Callback<List<CustomerResponse>>() {
            @Override
            public void onResponse(Call<List<CustomerResponse>> call, Response<List<CustomerResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CustomerResponse foundOp = null;
                    for (CustomerResponse u : response.body()) {
                        // So khớp TenDangNhap và Matkhau (Lưu ý: API dùng MatKhau hoặc Matkhau)
                        if (user.equals(u.getTenKhachHang()) && pass.equals(u.getMatKhau())) {
                            foundOp = u; break;
                        }
                    }

                    if (foundOp != null && "Nhaxe".equalsIgnoreCase(foundOp.getVaitro())) {
                        saveAndGo(foundOp.getUserID(), "operator");
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

    private void saveAndGo(String uid, String role) {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", role);
        
        if ("operator".equals(role)) {
            editor.putString("op_uid", uid); // QUAN TRỌNG: Lưu ID nhà xe để vào Profile lấy được dữ liệu
            startActivity(new Intent(this, OperatorMainActivity.class));
        } else {
            editor.putString("customerUid", uid);
            startActivity(new Intent(this, MainActivity.class));
        }
        editor.apply();
        finish();
    }
}
