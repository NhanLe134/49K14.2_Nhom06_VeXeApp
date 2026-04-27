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
        setupBottomNavigation(); 

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

        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navTickets = findViewById(R.id.nav_tickets);
        navFeedback = findViewById(R.id.nav_feedback);
    }

    private void setupBottomNavigation() {
        if (navHome != null) navHome.setOnClickListener(v -> finish());
        if (navTickets != null) navTickets.setOnClickListener(v -> checkLoginAndNavigate(QLVeXeActivity.class));
        if (navFeedback != null) navFeedback.setOnClickListener(v -> checkLoginAndNavigate(PhanHoiActivity.class));
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
        cities.add("Tất cả"); 
        cities.add("Đà Nẵng"); cities.add("Huế"); cities.add("Hội An");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOrigin.setAdapter(adapter);
        spDestination.setAdapter(adapter);
    }

    private void performSearch() {
        // Kiểm tra Selection tránh NullPointerException
        Object originObj = spOrigin.getSelectedItem();
        Object destObj = spDestination.getSelectedItem();
        
        String origin = originObj != null ? originObj.toString() : "Tất cả";
        String destination = destObj != null ? destObj.toString() : "Tất cả";
        String date = edtDate.getText().toString().trim();
        String time = edtTime.getText().toString().trim();

        if (!origin.equals("Tất cả") && !destination.equals("Tất cả") && origin.equals(destination)) {
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
