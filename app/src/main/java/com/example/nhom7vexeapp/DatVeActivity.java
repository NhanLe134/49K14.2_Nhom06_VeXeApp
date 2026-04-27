package com.example.nhom7vexeapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.models.Seat;
import com.example.nhom7vexeapp.models.TripSearchResult;
import com.example.nhom7vexeapp.models.Loaixe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatVeActivity extends AppCompatActivity {

    private TextView tvSoLuongChon, tvTongTien;
    private TextView tvDiemDiChon, tvDiemDenChon, tvNgayChon, tvGioChon;
    private GridLayout gridGheNgoi;
    private List<String> danhSachGheDaChon = new ArrayList<>();
    private Button btnTiepTuc;
    private TripSearchResult chuyenXeDaChon;
    private List<Seat> danhSachGheTuApi = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat_ve);

        chuyenXeDaChon = (TripSearchResult) getIntent().getSerializableExtra("selected_trip");

        khoiTaoGiaoDien();
        hienThiThongTinChuyenXe();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnTiepTuc.setOnClickListener(v -> {
            if (danhSachGheDaChon.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một chỗ ngồi!", Toast.LENGTH_SHORT).show();
            } else {
                Intent yDinhChuyenManHinh = new Intent(this, ThongTinChiTietActivity.class);
                yDinhChuyenManHinh.putExtra("selected_trip", chuyenXeDaChon);
                yDinhChuyenManHinh.putStringArrayListExtra("selected_seats", (ArrayList<String>) danhSachGheDaChon);
                yDinhChuyenManHinh.putExtra("total_price", tinhTongTien());
                startActivity(yDinhChuyenManHinh);
            }
        });

        kiemTraVaLayDanhSachGhe();
    }

    private void khoiTaoGiaoDien() {
        tvSoLuongChon = findViewById(R.id.tvSelectedCount);
        tvTongTien = findViewById(R.id.tvTotalPrice);
        tvDiemDiChon = findViewById(R.id.tvOriginSelect);
        tvDiemDenChon = findViewById(R.id.tvDestSelect);
        tvNgayChon = findViewById(R.id.tvDateSelect);
        tvGioChon = findViewById(R.id.tvTimeSelect);
        gridGheNgoi = findViewById(R.id.gridSeats);
        btnTiepTuc = findViewById(R.id.btnContinue);
    }

    private void kiemTraVaLayDanhSachGhe() {
        if (chuyenXeDaChon == null) return;
        com.example.nhom7vexeapp.api.ApiService apiService = ApiClient.getClient().create(com.example.nhom7vexeapp.api.ApiService.class);

        apiService.getLoaixe().enqueue(new Callback<List<Loaixe>>() {
            @Override
            public void onResponse(Call<List<Loaixe>> call, Response<List<Loaixe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int soGheGoc = -1;
                    String loaiXeCuaChuyen = chuyenXeDaChon.getCarType(); 

                    for (Loaixe lx : response.body()) {
                        if (loaiXeCuaChuyen.equalsIgnoreCase(lx.getLoaixeID()) || loaiXeCuaChuyen.contains(String.valueOf(lx.getSoCho()))) {
                            soGheGoc = lx.getSoCho();
                            break;
                        }
                    }

                    if (soGheGoc != -1) {
                        layVaLogGhe(soGheGoc);
                    } else {
                        Toast.makeText(DatVeActivity.this, "Lỗi: Không tìm thấy loại xe phù hợp!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Loaixe>> call, Throwable t) {
                Toast.makeText(DatVeActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void layVaLogGhe(int soGheGoc) {
        String maChuyenID = chuyenXeDaChon.getId();
        ApiClient.getClient().create(com.example.nhom7vexeapp.api.ApiService.class)
                .getSeatsByTrip(maChuyenID).enqueue(new Callback<List<Seat>>() {
            @Override
            public void onResponse(Call<List<Seat>> call, Response<List<Seat>> phanHoi) {
                if (phanHoi.isSuccessful() && phanHoi.body() != null) {
                    List<Seat> danhSachTuServer = phanHoi.body();
                    List<Seat> gheCuaChuyenNay = new ArrayList<>();
                    
                    for (Seat s : danhSachTuServer) {
                        if (s.getChuyenXe() == null || s.getChuyenXe().equals(maChuyenID)) {
                            gheCuaChuyenNay.add(s);
                        }
                    }
                    
                    if (gheCuaChuyenNay.size() == soGheGoc) {
                        danhSachGheTuApi.clear();
                        danhSachGheTuApi.addAll(gheCuaChuyenNay);
                        
                        // SẮP XẾP GHẾ THEO THỨ TỰ TỰ NHIÊN (A1, A2... B1, B2...)
                        Collections.sort(danhSachGheTuApi, new Comparator<Seat>() {
                            @Override
                            public int compare(Seat s1, Seat s2) {
                                String code1 = s1.getSeatCode();
                                String code2 = s2.getSeatCode();
                                if (code1 == null || code2 == null) return 0;
                                
                                String alpha1 = code1.replaceAll("\\d", "");
                                String alpha2 = code2.replaceAll("\\d", "");
                                
                                if (alpha1.equals(alpha2)) {
                                    try {
                                        int num1 = Integer.parseInt(code1.replaceAll("\\D", ""));
                                        int num2 = Integer.parseInt(code2.replaceAll("\\D", ""));
                                        return Integer.compare(num1, num2);
                                    } catch (Exception e) {
                                        return code1.compareTo(code2);
                                    }
                                }
                                return alpha1.compareTo(alpha2);
                            }
                        });

                        veSoDoGhe();
                    } else {
                        Toast.makeText(DatVeActivity.this, "Lỗi hệ thống: Số lượng ghế không khớp (" + gheCuaChuyenNay.size() + "/" + soGheGoc + ")", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Seat>> call, Throwable t) {
                Toast.makeText(DatVeActivity.this, "Lỗi kết nối server khi lấy ghế!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void veSoDoGhe() {
        if (gridGheNgoi == null) return;
        gridGheNgoi.removeAllViews();

        String loaiXe = chuyenXeDaChon.getCarType(); 
        String[][] soDo;
        int soCot = 3;

        if (loaiXe.contains("4")) {
            soDo = new String[][]{{"A", "O", "X"}, {"X", "X", "X"}};
        } else if (loaiXe.contains("7")) {
            soDo = new String[][]{{"A", "O", "X"}, {"X", "X", "X"}, {"X", "X", "X"}};
        } else if (loaiXe.contains("9")) {
            soDo = new String[][]{{"A", "X", "X"}, {"X", "O", "X"}, {"X", "O", "X"}, {"X", "X", "X"}};
        } else {
            soDo = new String[][]{{"A", "O", "X"}, {"X", "X", "X"}};
        }

        gridGheNgoi.setColumnCount(soCot);
        int index = 0;
        for (int i = 0; i < soDo.length; i++) {
            for (int j = 0; j < soDo[i].length; j++) {
                String kieu = soDo[i][j];
                View view = taoOItemGhe(kieu, index);
                if (kieu.equals("X")) index++;
                gridGheNgoi.addView(view);
            }
        }
    }

    private View taoOItemGhe(String kieuO, int chiSo) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(10, 10, 10, 10);
        ImageView anhGhe = new ImageView(this);
        anhGhe.setLayoutParams(new LinearLayout.LayoutParams(110, 110));
        TextView chuGhe = new TextView(this);
        chuGhe.setGravity(Gravity.CENTER);
        chuGhe.setTextSize(10);

        if (kieuO.equals("A")) {
            anhGhe.setImageResource(R.drawable.ic_steering_wheel); 
            anhGhe.setColorFilter(Color.DKGRAY);
            chuGhe.setText("Tài xế");
        } else if (kieuO.equals("X") && chiSo < danhSachGheTuApi.size()) {
            Seat ghe = danhSachGheTuApi.get(chiSo);
            anhGhe.setImageResource(R.drawable.ic_seat);
            chuGhe.setText(ghe.getSeatCode());

            String trangThai = ghe.getStatus() != null ? ghe.getStatus().trim() : "";
            if ("Đã đặt".equalsIgnoreCase(trangThai)) {
                anhGhe.setColorFilter(Color.GRAY);
                layout.setOnClickListener(v -> Toast.makeText(this, "Ghế đã có người đặt!", Toast.LENGTH_SHORT).show());
            } else {
                int mauXanh = Color.parseColor("#34B5F1");
                int mauVang = Color.parseColor("#FFEB3B");
                anhGhe.setColorFilter(danhSachGheDaChon.contains(ghe.getSeatCode()) ? mauVang : mauXanh);

                layout.setOnClickListener(v -> {
                    if (danhSachGheDaChon.contains(ghe.getSeatCode())) {
                        danhSachGheDaChon.remove(ghe.getSeatCode());
                        anhGhe.setColorFilter(mauXanh);
                    } else {
                        danhSachGheDaChon.add(ghe.getSeatCode());
                        anhGhe.setColorFilter(mauVang);
                    }
                    capNhatTomTat();
                });
            }
        } else {
            anhGhe.setVisibility(View.INVISIBLE);
            chuGhe.setVisibility(View.INVISIBLE);
        }
        layout.addView(anhGhe);
        layout.addView(chuGhe);
        return layout;
    }

    private void hienThiThongTinChuyenXe() {
        if (chuyenXeDaChon != null) {
            String tenTuyen = chuyenXeDaChon.getTuyenXeName();
            if (tenTuyen != null && tenTuyen.contains("-")) {
                String clear = tenTuyen.replace("Tuyến:", "").replace("Tuyến", "").trim();
                String[] p = clear.split("-");
                if (p.length >= 2) {
                    tvDiemDiChon.setText(p[0].trim());
                    tvDiemDenChon.setText(p[1].trim());
                }
            }
            tvNgayChon.setText(chuyenXeDaChon.getDate());
            tvGioChon.setText(chuyenXeDaChon.getTime());
        }
    }

    private long tinhTongTien() {
        if (chuyenXeDaChon == null) return 0;
        try {
            String p = chuyenXeDaChon.getPrice().replace(".", "").replace("đ", "").trim();
            return Long.parseLong(p) * danhSachGheDaChon.size();
        } catch (Exception e) { return 0; }
    }

    private void capNhatTomTat() {
        tvSoLuongChon.setText(String.valueOf(danhSachGheDaChon.size()));
        tvTongTien.setText(String.format("%,d đ", tinhTongTien()));
    }
}
