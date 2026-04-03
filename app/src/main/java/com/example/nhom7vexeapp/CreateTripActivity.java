package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Trip;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTripActivity extends AppCompatActivity {
    private Spinner spRoute, spTime, spVehicle;
    private EditText etDate;
    private TextView tvSeats, tvPrice, tvFormTitle;
    private LinearLayout layoutInfo;
    private Button btnSave, btnCancel;
    
    private String selectedRoute, selectedTime, selectedVehicle;
    private int selectedSeats;
    private String selectedPrice;

    private Trip editTrip;
    private int position = -1;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews();
        setupSpinners();
        setupDatePicker();
        setupListeners();

        editTrip = (Trip) getIntent().getSerializableExtra("editTrip");
        position = getIntent().getIntExtra("position", -1);

        if (editTrip != null) {
            populateFields();
        }
    }

    private void initViews() {
        spRoute = findViewById(R.id.spRoute);
        spTime = findViewById(R.id.spTime);
        spVehicle = findViewById(R.id.spVehicle);
        etDate = findViewById(R.id.etDate);
        tvSeats = findViewById(R.id.tvSeats);
        tvPrice = findViewById(R.id.tvPrice);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        layoutInfo = findViewById(R.id.layoutInfo);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupSpinners() {
        String[] routes = {"Chọn nơi xuất phát", "Huế-Đà Nẵng", "Đà Nẵng-Huế"};
        ArrayAdapter<String> routeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, routes);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRoute.setAdapter(routeAdapter);

        String[] times = new String[20];
        times[0] = "Chọn giờ xuất phát";
        for (int i = 4; i <= 22; i++) {
            times[i - 3] = i + "h00";
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTime.setAdapter(timeAdapter);

        String[] vehicles = {"Chọn loại xe", "xe 4 chỗ", "xe 7 chỗ", "xe limousine"};
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicles);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVehicle.setAdapter(vehicleAdapter);
    }

    private void populateFields() {
        if (tvFormTitle != null) tvFormTitle.setText("FORM CHỈNH SỬA THÔNG TIN CHUYẾN XE");
        setSpinnerSelection(spRoute, editTrip.getRouteName());
        etDate.setText(editTrip.getDate());
        setSpinnerSelection(spTime, editTrip.getTime());
        // Hiển thị loại xe nếu có
        setSpinnerSelection(spVehicle, editTrip.getVehicleType());
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.contains(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String date = String.format("%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                etDate.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        spRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoute = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTime = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVehicle = parent.getItemAtPosition(position).toString();
                if (position > 0) {
                    layoutInfo.setVisibility(View.VISIBLE);
                    if (position == 1) { selectedSeats = 4; selectedPrice = "150K"; }
                    else if (position == 2) { selectedSeats = 7; selectedPrice = "180K"; }
                    else { selectedSeats = 9; selectedPrice = "200K"; }
                    tvSeats.setText("Số ghế: " + selectedSeats);
                    tvPrice.setText("Giá vé: " + selectedPrice);
                } else {
                    layoutInfo.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> validateAndSave());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void validateAndSave() {
        if (selectedRoute.equals("Chọn nơi xuất phát") || etDate.getText().toString().isEmpty() || 
            selectedTime.equals("Chọn giờ xuất phát")) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Trip trip = new Trip(
            editTrip != null ? editTrip.getId() : "C" + (int)(Math.random() * 9000),
            selectedRoute,
            etDate.getText().toString(),
            selectedTime,
            editTrip != null ? editTrip.getStatus() : "Active"
        );

        saveTripToApi(trip);
    }

    private void saveTripToApi(Trip trip) {
        Toast.makeText(this, "Đang lưu lên server...", Toast.LENGTH_SHORT).show();
        apiService.createTrip(trip).enqueue(new Callback<Trip>() {
            @Override
            public void onResponse(Call<Trip> call, Response<Trip> response) {
                if (response.isSuccessful()) {
                    showSuccessDialog(trip);
                } else {
                    Toast.makeText(CreateTripActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Trip> call, Throwable t) {
                Toast.makeText(CreateTripActivity.this, "Lỗi kết nối Render!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog(Trip trip) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        TextView tvMsg = dialog.findViewById(R.id.tvMessage);
        tvMsg.setText(editTrip == null ? "Tạo chuyến xe thành công" : "Cập nhật thành công");
        dialog.show();

        new android.os.Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent();
            intent.putExtra("resultTrip", trip);
            setResult(RESULT_OK, intent);
            finish();
        }, 1500);
    }
}
