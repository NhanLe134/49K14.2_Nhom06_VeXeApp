package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.models.SearchHistory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SearchTicketActivity extends AppCompatActivity {

    private EditText edtDate, edtTime;
    private Spinner spOrigin, spDestination;
    private Button btnSearchTicket;
    private LinearLayout navHome, navSearch, navTickets, navFeedback, llRecentHistory;
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ticket);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        initViews();
        setupCityData();
        setupBottomNavigation(); 
        displayRecentHistory();

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
        llRecentHistory = findViewById(R.id.llRecentHistory);

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

        saveSearchHistory(origin, destination, date, time);

        Intent intent = new Intent(this, VeResultsActivity.class);
        intent.putExtra("ORIGIN_KEY", origin);
        intent.putExtra("DESTINATION_KEY", destination);
        intent.putExtra("DATE_KEY", date);
        intent.putExtra("TIME_KEY", time);
        startActivity(intent);
    }

    private void saveSearchHistory(String origin, String dest, String date, String time) {
        List<SearchHistory> history = getHistory();
        SearchHistory newItem = new SearchHistory(origin, dest, date, time);
        
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).equals(newItem)) {
                history.remove(i);
                break;
            }
        }
        
        history.add(0, newItem);
        if (history.size() > 5) history.remove(5);

        String json = gson.toJson(history);
        sharedPreferences.edit().putString("search_history", json).apply();
        displayRecentHistory();
    }

    private List<SearchHistory> getHistory() {
        String json = sharedPreferences.getString("search_history", "");
        if (json.isEmpty()) return new ArrayList<>();
        Type type = new TypeToken<List<SearchHistory>>(){}.getType();
        return gson.fromJson(json, type);
    }

    private void displayRecentHistory() {
        llRecentHistory.removeAllViews();
        List<SearchHistory> history = getHistory();
        
        for (SearchHistory item : history) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_search_history, llRecentHistory, false);
            TextView tvRoute = card.findViewById(R.id.tvHistoryRoute);
            TextView tvDetail = card.findViewById(R.id.tvHistoryDetail);
            
            tvRoute.setText(item.getOrigin() + " → " + item.getDest());
            String dateText = item.getDate().isEmpty() ? "Tất cả ngày" : item.getDate();
            String timeText = item.getTime().isEmpty() ? "" : " - " + item.getTime();
            tvDetail.setText(dateText + timeText);

            card.setOnClickListener(v -> {
                selectSpinnerItem(spOrigin, item.getOrigin());
                selectSpinnerItem(spDestination, item.getDest());
                edtDate.setText(item.getDate());
                edtTime.setText(item.getTime());
                performSearch();
            });

            llRecentHistory.addView(card);
        }
    }

    private void selectSpinnerItem(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
