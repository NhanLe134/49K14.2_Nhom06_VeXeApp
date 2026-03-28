package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class OperatorProfileActivity extends AppCompatActivity {

    private TextView tvOpName, tvOpRep, tvOpAddress, tvOpPhone, tvOpEmail, tvOpNameMain;
    private ImageView btnBack;
    private MaterialButton btnEdit, btnLogout;
    private LinearLayout navHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_profile);

        initViews();
        loadOperatorData();

        // Nút Back ở Header
        btnBack.setOnClickListener(v -> finish());

        // Nút Home ở Bottom Nav - Quay lại trang chủ
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, OperatorMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditOperatorProfileActivity.class);
            startActivityForResult(intent, 400);
        });

        // Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.remove("op_user");
            editor.apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        tvOpNameMain = findViewById(R.id.tvOpNameMain);
        tvOpName = findViewById(R.id.tvOpName);
        tvOpRep = findViewById(R.id.tvOpRep);
        tvOpAddress = findViewById(R.id.tvOpAddress);
        tvOpPhone = findViewById(R.id.tvOpPhone);
        tvOpEmail = findViewById(R.id.tvOpEmail);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEditOpProfile);
        btnLogout = findViewById(R.id.btnLogoutOp);
        navHome = findViewById(R.id.navHomeProfile);
    }

    private void loadOperatorData() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = pref.getString("op_name", "Nhà xe Đà Nẵng-Huế");
        tvOpNameMain.setText(name);
        tvOpName.setText(name);
        tvOpRep.setText(pref.getString("op_rep", "Tôn Thất Huy Phong"));
        tvOpAddress.setText(pref.getString("op_address", "K36/12 Lưu Quang Thuận, Đà Nẵng"));
        tvOpPhone.setText(pref.getString("op_phone", "0905509767"));
        tvOpEmail.setText(pref.getString("op_email", "dananghue@nhaxe.vn"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 400 && resultCode == RESULT_OK) {
            loadOperatorData();
        }
    }
}
