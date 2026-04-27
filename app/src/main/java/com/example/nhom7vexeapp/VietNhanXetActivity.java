package com.example.nhom7vexeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.TripSearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// #Trang
public class VietNhanXetActivity extends AppCompatActivity {

    private static final String TAG = "VietNhanXet";
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private String ticketId, tripId;
    private TextView tvBusName, tvRoute, tvDateTime;
    private RatingBar ratingBar;
    private EditText edtComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viet_nhan_xet);

        apiService = ApiClient.getClient().create(ApiService.class);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        tvBusName = findViewById(R.id.tvBusNameReview);
        tvRoute = findViewById(R.id.tvRouteReview);
        tvDateTime = findViewById(R.id.tvDateTimeReview);
        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtComment);
        Button btnSubmit = findViewById(R.id.btnSubmitReview);
        
        ratingBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));

        Intent intent = getIntent();
        if (intent != null) {
            ticketId = intent.getStringExtra("ticket_id");
            tripId = intent.getStringExtra("trip_id");
            tvBusName.setText(intent.getStringExtra("bus_company"));
            tvRoute.setText(intent.getStringExtra("route"));
            tvDateTime.setText(intent.getStringExtra("date_time"));
            if (tripId != null && !tripId.isEmpty()) {
                fetchRealTripData(tripId, intent.getStringExtra("date_time"));
            }
        }

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = edtComment.getText().toString().trim();
            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ticketId == null || ticketId.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy thông tin vé!", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchNextIdAndSubmit(rating, comment);
        });

        findViewById(R.id.nav_home_review).setOnClickListener(v -> finish());
    }

    private void fetchNextIdAndSubmit(float rating, String comment) {
        apiService.getFeedbacks().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                String nextId = "DG" + (System.currentTimeMillis() % 1000000);
                if (response.isSuccessful() && response.body() != null) {
                    int max = 0;
                    for (Map<String, Object> fb : response.body()) {
                        Object idObj = fb.get("DanhGiaID");
                        if (idObj != null) {
                            String idStr = idObj.toString();
                            if (idStr.startsWith("DG")) {
                                try {
                                    int num = Integer.parseInt(idStr.substring(2));
                                    if (num > max) max = num;
                                } catch (Exception e) {}
                            }
                        }
                    }
                    nextId = String.format("DG%05d", max + 1);
                }
                submitFeedback(nextId, rating, comment);
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                submitFeedback("DG" + (System.currentTimeMillis() % 100000), rating, comment);
            }
        });
    }

    private void submitFeedback(String id, float rating, String comment) {
        String khId = sharedPreferences.getString("customerUid", "");
        
        if (khId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID khách hàng. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("DanhGiaID", id);
        data.put("Ve", ticketId);
        data.put("KhachHang", khId);
        data.put("Diemso", (int)rating);
        data.put("Nhanxet", comment);

        Log.e(TAG, "DỮ LIỆU GỬI LÊN SERVER:");
        Log.e(TAG, data.toString());

        apiService.sendFeedback(data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    updateTicketStatusOnServer();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception e) {}
                    Log.e(TAG, "GỬI THẤT BẠI: " + response.code() + " - " + errorBody);
                    Toast.makeText(VietNhanXetActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "LỖI KẾT NỐI: " + t.getMessage());
                Toast.makeText(VietNhanXetActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTicketStatusOnServer() {
        Map<String, Object> patch = new HashMap<>();
        patch.put("TrangThaiDanhGia", "Đã đánh giá");
        apiService.patchTicket(ticketId, patch).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) { showSuccessDialog(); }
            @Override public void onFailure(Call<Void> call, Throwable t) { showSuccessDialog(); }
        });
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        TextView tv = view.findViewById(R.id.tvMessage);
        if (tv != null) tv.setText("Gửi đánh giá thành công");
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        new Handler().postDelayed(() -> { if (dialog.isShowing()) dialog.dismiss(); finish(); }, 2000);
    }

    private void fetchRealTripData(String id, String originalDateTime) {
        apiService.getChuyenXe().enqueue(new Callback<List<TripSearchResult>>() {
            @Override
            public void onResponse(Call<List<TripSearchResult>> call, Response<List<TripSearchResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (TripSearchResult t : response.body()) {
                        if (id.equals(t.getId())) {
                            String start = cleanTime(t.getTime());
                            String end = cleanTime(t.getEndTime());
                            String datePart = originalDateTime.contains(" ") ? originalDateTime.split(" ")[0] : originalDateTime;
                            tvDateTime.setText(datePart + "  " + start + " - " + end);
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<List<TripSearchResult>> call, Throwable t) {}
        });
    }

    private String cleanTime(String t) {
        if (t == null || t.isEmpty()) return "00:00";
        return t.contains(":") ? t.substring(0, 5) : t;
    }
}
