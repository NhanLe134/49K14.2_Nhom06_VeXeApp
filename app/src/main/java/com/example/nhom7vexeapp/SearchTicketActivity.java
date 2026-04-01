package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchTicketActivity extends AppCompatActivity {

    private EditText edtDate;
    private Spinner spOrigin, spDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ticket);

        // 1. Ánh xạ các View từ XML
        edtDate = findViewById(R.id.edtDate);
        spOrigin = findViewById(R.id.spOrigin);
        spDestination = findViewById(R.id.spDestination);

        // 2. Đổ dữ liệu vào Spinner (Dropdown)
        setupCityData();

        // 3. Sự kiện chọn ngày đi
        if (edtDate != null) {
            edtDate.setOnClickListener(v -> openDatePicker());
        }
    }

    private void setupCityData() {
        // Tạo danh sách 3 địa điểm như yêu cầu của Xù
        List<String> cities = new ArrayList<>();
        cities.add("Đà Nẵng");
        cities.add("Huế");
        cities.add("Hội An");

        // Tạo Adapter (người kết nối dữ liệu và giao diện)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cities);

        // Thiết lập kiểu hiển thị khi bấm mở danh sách
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Gán dữ liệu cho cả 2 Spinner
        spOrigin.setAdapter(adapter);
        spDestination.setAdapter(adapter);

        // Mặc định chọn Huế ở Spinner "Nơi đến" cho khác biệt
        spDestination.setSelection(1);
    }

    private void openDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Hiển thị ngày đã chọn lên ô nhập
                    String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    edtDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }
}