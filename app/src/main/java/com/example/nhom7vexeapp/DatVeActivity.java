package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class DatVeActivity extends AppCompatActivity {

    private TextView tvSelectedCount, tvTotalPrice;
    private List<String> selectedSeats = new ArrayList<>();
    private final long PRICE_PER_SEAT = 90000;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat_ve);

        // Ánh xạ View
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnContinue = findViewById(R.id.btnContinue);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                if (selectedSeats.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn ít nhất một chỗ ngồi!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(DatVeActivity.this, ThongTinChiTietActivity.class);
                    // Truyền thông tin ghế đã chọn sang màn hình tiếp theo
                    intent.putStringArrayListExtra("selected_seats", (ArrayList<String>) selectedSeats);
                    startActivity(intent);
                }
            });
        }

        // Thiết lập logic cho từng ghế
        setupSeat(R.id.seat_B5, "B5", true);
        setupSeat(R.id.seat_B7, "B7", true);
        setupSeat(R.id.seat_B9, "B9", true);
        setupSeat(R.id.seat_B6, "B6", false);
        setupSeat(R.id.seat_B8, "B8", false);
        setupSeat(R.id.seat_B10, "B10", false);
    }

    private void setupSeat(int layoutId, String seatCode, boolean isSold) {
        LinearLayout layout = findViewById(layoutId);
        if (layout == null) return;

        ImageView imgSeat = (ImageView) layout.getChildAt(0);

        if (isSold) {
            imgSeat.setColorFilter(Color.parseColor("#888888"));
            layout.setOnClickListener(v -> 
                Toast.makeText(this, "Ghế " + seatCode + " này đã có người đặt!", Toast.LENGTH_SHORT).show()
            );
        } else {
            imgSeat.setColorFilter(Color.parseColor("#34B5F1"));
            layout.setOnClickListener(v -> {
                if (selectedSeats.contains(seatCode)) {
                    selectedSeats.remove(seatCode);
                    imgSeat.setColorFilter(Color.parseColor("#34B5F1"));
                } else {
                    selectedSeats.add(seatCode);
                    imgSeat.setColorFilter(Color.parseColor("#FFD700"));
                }
                updateSummary();
            });
        }
    }

    private void updateSummary() {
        int count = selectedSeats.size();
        long total = count * PRICE_PER_SEAT;
        tvSelectedCount.setText(String.valueOf(count));
        tvTotalPrice.setText(String.format("%,d vnđ", total));
    }
}
