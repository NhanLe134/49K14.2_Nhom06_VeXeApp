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

import com.example.nhom7vexeapp.models.LoginRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private boolean isOperatorMode = false;
    private LinearLayout layoutCustomerLogin, layoutOperatorLogin;
    private TextView tvLoginTitle, tvSwitchMode, tvRegisterCustomer, tvRegisterOperator;
    private EditText edtPhoneLogin, edtUsername, edtPassword;
    private Button btnLoginCustomer, btnLoginOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

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
        
        // Nhận SĐT vừa đăng ký xong (nếu có)
        String registeredPhone = getIntent().getStringExtra("registeredPhone");
        if (registeredPhone != null) {
            edtPhoneLogin.setText(registeredPhone);
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
        String phone = edtPhoneLogin.getText().toString().trim();
        if (phone.length() >= 10) {
            // Hiện tại API user-auth thường dùng username/password. 
            // Nếu bạn muốn đăng nhập bằng SĐT qua API, hãy sử dụng logic tương tự handleOperatorLogin
            // Ở đây tôi giữ logic cũ hoặc bạn có thể đổi thành gọi API
            
            SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            
            String name, dob;
            
            // Mock data cho khách hàng
            if (phone.equals("0799376815")) {
                name = "Huy Phong";
                dob = "03/10/2005";
            } else {
                name = pref.getString("name_" + phone, "Khách hàng mới");
                dob = pref.getString("dob_" + phone, "01/01/2000");
            }
            
            editor.putBoolean("isLoggedIn", true);
            editor.putString("customerPhone", phone);
            editor.putString("customerName", name);
            editor.putString("customerDob", dob);
            editor.putString("userRole", "customer");
            editor.apply();

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Vui lòng nhập số điện thoại hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleOperatorLogin() {
        String user = edtUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API Đăng nhập qua Retrofit
        LoginRequest loginRequest = new LoginRequest(user, pass);
        RetrofitClient.getApiService().login(loginRequest).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User userInfo = response.body();
                    
                    // Lưu trạng thái đăng nhập vào SharedPreferences
                    SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("userRole", userInfo.getRole());
                    editor.putString("userEmail", userInfo.getEmail());
                    editor.apply();

                    // Chuyển màn hình dựa trên Role (nhà xe hoặc khách hàng)
                    if ("operator".equals(userInfo.getRole())) {
                        startActivity(new Intent(LoginActivity.this, OperatorMainActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    finish();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API_ERROR", "Login failed: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
