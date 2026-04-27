package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.BookingRequest;
import com.example.nhom7vexeapp.models.TripSearchResult;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThongTinChiTietActivity extends AppCompatActivity {

    private EditText edtPickUp, edtDropOff, edtCustomerName, edtPhone;
    private TextView tvOriginDetail, tvDestDetail, tvDateDetail, tvTimeDetail;
    private Button btnContinue;
    private ImageView btnBack;
    private TripSearchResult selectedTrip;
    private ArrayList<String> selectedSeats;
    private long totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_chi_tiet);

        Intent intent = getIntent();
        selectedTrip = (TripSearchResult) intent.getSerializableExtra("selected_trip");
        selectedSeats = intent.getStringArrayListExtra("selected_seats");
        totalPrice = intent.getLongExtra("total_price", 0);

        if (selectedTrip == null || selectedSeats == null) {
            Toast.makeText(this, "Lỗi dữ liệu chuyến đi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayTripSummary();
        loadCustomerInfo();

        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            if (validateInfo()) {
                performBooking();
            }
        });
    }

    private void initViews() {
        edtPickUp = findViewById(R.id.edtPickUp);
        edtDropOff = findViewById(R.id.edtDropOff);
        edtCustomerName = findViewById(R.id.edtCustomerName);
        edtPhone = findViewById(R.id.edtPhone);
        tvOriginDetail = findViewById(R.id.tvOriginDetail);
        tvDestDetail = findViewById(R.id.tvDestDetail);
        tvDateDetail = findViewById(R.id.tvDateDetail);
        tvTimeDetail = findViewById(R.id.tvTimeDetail);
        btnContinue = findViewById(R.id.btnContinue);
        btnBack = findViewById(R.id.btnBack);
    }

    private void displayTripSummary() {
        if (selectedTrip != null) {
            String routeName = selectedTrip.getTuyenXeName();
            if (routeName != null) {
                String cleanRoute = routeName.replace("Tuyến:", "").replace("Tuyến", "").trim();
                if (cleanRoute.contains("-")) {
                    String[] parts = cleanRoute.split("-");
                    if (tvOriginDetail != null) tvOriginDetail.setText(parts[0].trim());
                    if (tvDestDetail != null) tvDestDetail.setText(parts[1].trim());
                } else {
                    if (tvOriginDetail != null) tvOriginDetail.setText(cleanRoute);
                }
            }
            if (tvDateDetail != null) tvDateDetail.setText(selectedTrip.getDate());
            String timeDisplay = selectedTrip.getTime(); 
            if (selectedTrip.getEndTime() != null) {
                timeDisplay += " - " + selectedTrip.getEndTime();
            }
            if (tvTimeDetail != null) tvTimeDetail.setText(timeDisplay);
        }
    }

    private void loadCustomerInfo() {
        SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = pref.getString("customerName", "");
        String phone = pref.getString("customerPhone", "");
        edtCustomerName.setText(name);
        edtPhone.setText(phone);
    }

    private boolean validateInfo() {
        if (edtCustomerName.getText().toString().trim().isEmpty() ||
                edtPhone.getText().toString().trim().isEmpty() ||
                edtPickUp.getText().toString().trim().isEmpty() ||
                edtDropOff.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performBooking() {
        String inputPhone = edtPhone.getText().toString().trim();
        String pickUp = edtPickUp.getText().toString().trim();
        String dropOff = edtDropOff.getText().toString().trim();

        // LOGIC VEID: mã ghế đầu tiên + id chuyến xe
        String firstSeat = selectedSeats.get(0);
        String veId = firstSeat + selectedTrip.getId();

        btnContinue.setEnabled(false);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        BookingRequest request = new BookingRequest(
                veId,
                selectedTrip.getId(),
                inputPhone,
                selectedSeats,
                totalPrice,
                pickUp,
                dropOff
        );

        apiService.bookTicket(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnContinue.setEnabled(true);
                if (response.isSuccessful()) {
                    showSuccessDialog("Đặt vé thành công!");
                } else {
                    try {
                        String errorLog = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("BOOKING_ERROR", "Code: " + response.code() + " | Response: " + errorLog);
                        Toast.makeText(ThongTinChiTietActivity.this, "Đặt vé thất bại: " + errorLog, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnContinue.setEnabled(true);
                Log.e("BOOKING_ERROR", "Network failure: " + t.getMessage());
                Toast.makeText(ThongTinChiTietActivity.this, "Lỗi kết nối server!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo");
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent(ThongTinChiTietActivity.this, QLVeXeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }
}
