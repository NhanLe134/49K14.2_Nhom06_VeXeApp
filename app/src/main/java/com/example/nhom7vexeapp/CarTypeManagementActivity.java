package com.example.nhom7vexeapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom7vexeapp.adapters.CarTypeAdapter;
import com.example.nhom7vexeapp.models.CarType;

import java.util.ArrayList;
import java.util.List;

public class CarTypeManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_type_management);

        // 1. Ánh xạ RecyclerView từ XML
        RecyclerView rv = findViewById(R.id.rvCarTypes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 2. Chuẩn bị dữ liệu mẫu
        List<CarType> data = new ArrayList<>();
        data.add(new CarType("Loại xe A", 4, "150.000 đ", "25/2/2026", Color.parseColor("#0091D5")));
        data.add(new CarType("Loại xe B", 7, "250.000 đ", "20/2/2026", Color.parseColor("#10B981")));
        data.add(new CarType("Loại xe C", 9, "Chưa thiết lập", "Chưa cập nhật", Color.parseColor("#8B5CF6")));

        // 3. Đổ dữ liệu vào Adapter và hiển thị
        CarTypeAdapter adapter = new CarTypeAdapter(data, this);
        rv.setAdapter(adapter);
    }
}