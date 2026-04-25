package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchTicketActivity extends AppCompatActivity {

    private EditText edtDate, edtTime;
    private Spinner spOrigin, spDestination;
    private Button btnSearchTicket;
    private LinearLayout navHome, navSearch, navTickets, navFeedback;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ticket);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        initViews();
        setupCityData();
        setCurrentDateTime();
        setupBottomNavigation(); // Cài đặt footer

        edtDate.setOnClickListener(v -> openDatePicker());
        edtTime.setOnClickListener(v -> openTimePicker());
        btnSearchTicket.setOnClickListener(v -> performSearch());
    }

    private void initViews() {
        edtDate = findViewById(R.id.edtDate);
        edtTime = findViewById(R.id.edtTime);
        spOrigin = findViewById(R.id.spOrigin);
        spDestination = findViewById(R.id.spDestination);
        btnSearchTicket = findViewById(R.id.btnSearchTicket);

        // Ánh xạ Footer
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets);
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void setupBottomNavigation() {
        // 1. Về Trang chủ
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }

        // 2. Tab Tìm kiếm (Hiện tại)
        if (navSearch != null) {
            navSearch.setOnClickListener(v -> {
                // Đang ở trang tìm kiếm
            });
        }

        // 3. Xem Vé của tôi (Yêu cầu đăng nhập)
        if (navTickets != null) {
            navTickets.setOnClickListener(v -> checkLoginAndNavigate(QLVeXeActivity.class));
        }

        // 4. Xem Đánh giá (Yêu cầu đăng nhập)
        if (navFeedback != null) {
            navFeedback.setOnClickListener(v -> checkLoginAndNavigate(PhanHoiActivity.class));
        }
    }

    private void checkLoginAndNavigate(Class<?> targetActivity) {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            startActivity(new Intent(this, targetActivity));
        } else {
            showLoginRequiredDialog();
        }
    }

    private void showLoginRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage("Bạn cần đăng nhập để thực hiện chức năng này.")
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                })
                .setNegativeButton("Để sau", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        edtDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d",
                c.get(Calendar.DAY_OF_MONTH), (c.get(Calendar.MONTH) + 1), c.get(Calendar.YEAR)));
        edtTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            edtDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", d, (m + 1), y));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, h, m) -> {
            edtTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void setupCityData() {
        List<String> cities = new ArrayList<>();
        cities.add("Đà Nẵng"); cities.add("Huế"); cities.add("Hội An");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOrigin.setAdapter(adapter);
        spDestination.setAdapter(adapter);
        spDestination.setSelection(1);
    }

    private void performSearch() {
        String origin = spOrigin.getSelectedItem().toString();
        String destination = spDestination.getSelectedItem().toString();
        String date = edtDate.getText().toString();
        String time = edtTime.getText().toString();

        if (origin.equals(destination)) {
            Toast.makeText(this, "Nơi đi và đến phải khác nhau!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, VeResultsActivity.class);
        intent.putExtra("ORIGIN_KEY", origin);
        intent.putExtra("DESTINATION_KEY", destination);
        intent.putExtra("DATE_KEY", date);
        intent.putExtra("TIME_KEY", time);
        startActivity(intent);
    }
}
