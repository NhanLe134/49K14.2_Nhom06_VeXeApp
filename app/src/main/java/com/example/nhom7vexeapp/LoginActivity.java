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
import com.example.nhom7vexeapp.models.UserModel;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private boolean isOperatorMode = false;
    private LinearLayout layoutCustomerLogin, layoutOperatorLogin;
    private TextView tvLoginTitle, tvSwitchMode;
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

        View regCust = findViewById(R.id.tvRegisterCustomer);
        if (regCust != null) regCust.setOnClickListener(v -> startActivity(new Intent(this, CustomerRegisterActivity.class)));
        
        if (btnLoginCustomer != null) btnLoginCustomer.setOnClickListener(v -> handleCustomerLogin());

        View regOp = findViewById(R.id.tvRegisterOperator);
        if (regOp != null) regOp.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        
        if (btnLoginOperator != null) btnLoginOperator.setOnClickListener(v -> handleOperatorLogin());
    }

    private void initViews() {
        layoutCustomerLogin = findViewById(R.id.layoutCustomerLogin);
        layoutOperatorLogin = findViewById(R.id.layoutOperatorLogin);
        tvLoginTitle = findViewById(R.id.tvLoginTitle);
        tvSwitchMode = findViewById(R.id.tvSwitchMode);
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
        if (tvSwitchMode != null) tvSwitchMode.setText(isOperatorMode ? "Bạn là khách hàng?" : "Bạn là nhà xe?");
    }

    private void handleCustomerLogin() {
        String phone = edtPhoneLogin.getText().toString().trim();
        if (phone.isEmpty()) { Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show(); return; }
        
        apiService.getUsers("Get").enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel found = null;
                    for (UserModel u : response.body()) {
                        if (phone.equals(u.getSoDienThoai())) { found = u; break; }
                    }
                    if (found != null) {
                        String realKhId = found.getKhachHang();
                        if (realKhId == null || realKhId.isEmpty()) realKhId = found.getUserID();
                        saveAndGo(found.getUserID(), "customer", phone, realKhId);
                    } else {
                        Toast.makeText(LoginActivity.this, "Số điện thoại chưa đăng ký!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleOperatorLogin() {
        String user = edtUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();
        if (user.isEmpty() || pass.isEmpty()) { Toast.makeText(this, "Nhập đủ tài khoản và mật khẩu!", Toast.LENGTH_SHORT).show(); return; }
        
        apiService.getUsers("Get").enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserModel found = null;
                    for (UserModel u : response.body()) {
                        if (user.equals(u.getTenDangNhap()) && pass.equals(u.getMatKhau())) { found = u; break; }
                    }
                    if (found != null && "Nhaxe".equalsIgnoreCase(found.getVaitro())) {
                        String realOpId = found.getNhaxe();
                        saveAndGo(found.getUserID(), "operator", user, realOpId);
                    } else {
                        Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAndGo(String uid, String role, String user, String targetId) {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", role);
        
        if ("operator".equals(role)) {
            editor.putString("op_uid", (targetId != null && !targetId.isEmpty()) ? targetId : uid);
            editor.putString("op_user", user);
            startActivity(new Intent(this, OperatorMainActivity.class));
        } else {
            editor.putString("customerUid", targetId);
            editor.putString("user_id", uid);
            editor.putString("customerPhone", user);
            startActivity(new Intent(this, MainActivity.class));
        }
        editor.apply();
        finish();
    }
}
