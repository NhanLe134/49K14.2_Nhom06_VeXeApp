package com.example.nhom7vexeapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom7vexeapp.api.ApiClient;
import com.example.nhom7vexeapp.api.ApiService;
import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.Route;
import com.example.nhom7vexeapp.models.Trip;
import com.example.nhom7vexeapp.models.VehicleManaged;

import java.util.*;
import java.text.SimpleDateFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTripActivity extends AppCompatActivity {

    private Spinner spRoute, spTime, spVehicle;
    private EditText etDate;
    private TextView tvSeats, tvPrice, tvFormTitle;
    private LinearLayout layoutInfo;
    private Button btnSave, btnCancel;

    private String selectedRouteId = "", selectedVehicleId = "", selectedTime = "";
    private String currentPrice = "0", currentSeats = "40";

    private ApiService apiService;
    private Trip editingTrip;

    private List<String> routeNames = new ArrayList<>(), routeIds = new ArrayList<>();
    private List<String> vehicleNames = new ArrayList<>(), vehicleIds = new ArrayList<>();
    private List<VehicleManaged> fullVehicles = new ArrayList<>();
    private List<Route> fullRoutes = new ArrayList<>();
    private List<Loaixe> carTypeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews();

        editingTrip = (Trip) getIntent().getSerializableExtra("editTrip");
        if (editingTrip != null) {
            tvFormTitle.setText("CHỈNH SỬA THÔNG TIN CHUYẾN XE");
            btnSave.setText("Cập nhật");
            etDate.setText(editingTrip.getDate());
            selectedRouteId = editingTrip.getTuyenXeID();
            selectedVehicleId = editingTrip.getXeID();
            selectedTime = editingTrip.getTime();
        }

        loadCarTypes();
        loadRoutesFromServer();
        loadVehiclesFromServer();
        setupTimeSpinner();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        tvFormTitle = findViewById(R.id.tvFormTitle);
        spRoute = findViewById(R.id.spRoute);
        spTime = findViewById(R.id.spTime);
        spVehicle = findViewById(R.id.spVehicle);
        etDate = findViewById(R.id.etDate);
        tvSeats = findViewById(R.id.tvSeats);
        tvPrice = findViewById(R.id.tvPrice);
        layoutInfo = findViewById(R.id.layoutInfo);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void loadCarTypes() {
        apiService.getLoaixe().enqueue(new Callback<List<Loaixe>>() {
            @Override
            public void onResponse(Call<List<Loaixe>> call, Response<List<Loaixe>> response) {
                if (response.isSuccessful() && response.body() != null) carTypeList = response.body();
            }
            @Override public void onFailure(Call<List<Loaixe>> call, Throwable t) {}
        });
    }

    private void loadRoutesFromServer() {
        apiService.getRoutes().enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                routeNames.clear(); routeIds.clear(); fullRoutes.clear();
                routeNames.add("Chọn tuyến xe"); routeIds.add(""); fullRoutes.add(null);

                if (response.isSuccessful() && response.body() != null) {
                    for (Route r : response.body()) {
                        String id = r.getId();
                        String name = r.getName();
                        if (id != null && !id.isEmpty()) {
                            routeNames.add(name + " (" + id + ")");
                            routeIds.add(id);
                            fullRoutes.add(r);
                        }
                    }
                }
                updateSpinner(spRoute, routeNames);
                if (editingTrip != null) setSpinnerSelection(spRoute, routeIds, editingTrip.getTuyenXeID());
            }
            @Override public void onFailure(Call<List<Route>> call, Throwable t) {}
        });
    }

    private void loadVehiclesFromServer() {
        apiService.getVehicles().enqueue(new Callback<List<VehicleManaged>>() {
            @Override
            public void onResponse(Call<List<VehicleManaged>> call, Response<List<VehicleManaged>> response) {
                vehicleNames.clear(); vehicleIds.clear(); fullVehicles.clear();
                vehicleNames.add("Chọn xe"); vehicleIds.add("");
                fullVehicles.add(null);
                if (response.isSuccessful() && response.body() != null) {
                    for (VehicleManaged v : response.body()) {
                        String id = v.getXeID();
                        String bs = v.getBienSoXe();
                        if (id != null && !id.isEmpty()) {
                            vehicleNames.add(bs + " (" + id + ")");
                            vehicleIds.add(id);
                            fullVehicles.add(v);
                        }
                    }
                }
                updateSpinner(spVehicle, vehicleNames);
                if (editingTrip != null) setSpinnerSelection(spVehicle, vehicleIds, editingTrip.getXeID());
            }
            @Override public void onFailure(Call<List<VehicleManaged>> call, Throwable t) {}
        });
    }

    private void setupListeners() {
        spRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedRouteId = routeIds.get(pos);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        spVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedVehicleId = vehicleIds.get(pos);
                updateVehicleInfo(pos);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedTime = p.getItemAtPosition(pos).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        btnSave.setOnClickListener(v -> validateAndSave());
        btnCancel.setOnClickListener(v -> showConfirmCancelDialog());
    }

    private void validateAndSave() {
        String dateStr = etDate.getText().toString().trim();
        if (selectedRouteId.isEmpty() || dateStr.isEmpty() || selectedTime.contains("Chọn") || selectedVehicleId.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getTrips().enqueue(new Callback<List<Trip>>() {
            @Override
            public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                String finalTripId;
                if (editingTrip != null) {
                    finalTripId = editingTrip.getId();
                } else {
                    int maxNum = 0;
                    if (response.isSuccessful() && response.body() != null) {
                        for (Trip t : response.body()) {
                            try {
                                String id = t.getId();
                                if (id != null) {
                                    String numOnly = id.replaceAll("[^0-9]", "");
                                    if (!numOnly.isEmpty()) {
                                        int val = Integer.parseInt(numOnly);
                                        if (val > maxNum) maxNum = val;
                                    }
                                }
                            } catch (Exception e) {}
                        }
                    }
                    finalTripId = String.format("CX%05d", maxNum + 1);
                }
                saveTripToServer(finalTripId, dateStr);
            }
            @Override public void onFailure(Call<List<Trip>> call, Throwable t) {
                if (editingTrip != null) {
                    saveTripToServer(editingTrip.getId(), dateStr);
                } else {
                    saveTripToServer("CX00001", dateStr);
                }
            }
        });
    }

    private void saveTripToServer(String tripId, String dateStr) {
        Map<String, Object> data = new HashMap<>();
        String gioDen = calculateEndTime(selectedTime);

        data.put("ChuyenXeID", tripId);
        data.put("NgayKhoiHanh", dateStr);
        data.put("GioDi", selectedTime);
        data.put("GioDen", gioDen);
        data.put("Xe", selectedVehicleId);
        data.put("TuyenXe", selectedRouteId);
        data.put("TrangThai", (editingTrip != null) ? editingTrip.getStatus() : "Chưa hoàn thành");

        if (editingTrip != null && editingTrip.getTaiXeID() != null && !editingTrip.getTaiXeID().isEmpty()) {
            data.put("Taixe", editingTrip.getTaiXeID());
        } else {
            data.put("Taixe", null);
        }

        Call<Void> call = (editingTrip != null) ? apiService.updateTripRaw(tripId, data) : apiService.createTripRaw(data);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (editingTrip == null) {
                        createSeatsForNewTrip(tripId);
                    } else {
                        showSuccessDialog(true);
                    }
                } else {
                    Toast.makeText(CreateTripActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateTripActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createSeatsForNewTrip(String tripId) {
        int numSeats = 40;
        try { numSeats = Integer.parseInt(currentSeats); } catch (Exception e) {}

        String prefix = "G";
        if (numSeats == 4) prefix = "A";
        else if (numSeats == 7) prefix = "B";
        else if (numSeats == 9) prefix = "C";

        for (int i = 1; i <= numSeats; i++) {
            String maGhe = (numSeats >= 10) ? String.format("%s%02d", prefix, i) : prefix + i;
            String gheId = tripId + maGhe;

            Map<String, Object> gheData = new HashMap<>();
            gheData.put("gheID", gheId);
            gheData.put("ChuyenXe", tripId);
            gheData.put("soGhe", maGhe);
            gheData.put("trangThai", "Còn trống");

            apiService.createGheNgoi(gheData).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {}
                @Override public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
        showSuccessDialog(false);
    }

    private String calculateEndTime(String startTime) {
        try {
            int routePos = spRoute.getSelectedItemPosition();
            String durationStr = "02:00:00";
            if (routePos > 0 && routePos < fullRoutes.size()) {
                Route r = fullRoutes.get(routePos);
                if (r != null) durationStr = r.getTime();
            }
            int durH = 0, durM = 0;
            if (durationStr != null && durationStr.contains("h")) {
                String[] parts = durationStr.split("h");
                durH = Integer.parseInt(parts[0].trim());
                if (parts.length > 1 && !parts[1].isEmpty()) durM = Integer.parseInt(parts[1].trim());
            } else if (durationStr != null && durationStr.contains(":")) {
                String[] parts = durationStr.split(":");
                durH = Integer.parseInt(parts[0]);
                durM = Integer.parseInt(parts[1]);
            }
            String[] s = startTime.split(":");
            int h = Integer.parseInt(s[0]) + durH;
            int m = Integer.parseInt(s[1]) + durM;
            h += m / 60; m = m % 60;
            return String.format("%02d:%02d:00", h % 24, m);
        } catch (Exception e) { return startTime; }
    }

    private void updateVehicleInfo(int position) {
        if (position > 0 && position < fullVehicles.size()) {
            VehicleManaged vehicle = fullVehicles.get(position);
            if (vehicle != null) {
                String typeId = vehicle.getLoaiXeIDStr();
                Loaixe matchedType = null;
                for (Loaixe type : carTypeList) {
                    if (type.getLoaixeID().equalsIgnoreCase(typeId)) { matchedType = type; break; }
                }
                if (matchedType != null) {
                    currentSeats = String.valueOf(matchedType.getSoCho());
                    currentPrice = matchedType.getGiaVe();
                    tvSeats.setText("Số ghế: " + currentSeats);
                    tvPrice.setText("Giá vé: " + currentPrice + " VND");
                }
            }
            layoutInfo.setVisibility(View.VISIBLE);
        } else { layoutInfo.setVisibility(View.GONE); }
    }

    private void setupTimeSpinner() {
        List<String> times = new ArrayList<>();
        times.add("Chọn giờ đi");
        for (int i = 4; i <= 23; i++) times.add(String.format("%02d:00:00", i));
        updateSpinner(spTime, times);
        if (editingTrip != null && editingTrip.getTime() != null) {
            String t = editingTrip.getTime(); if (t.length() == 5) t += ":00";
            for (int i = 0; i < times.size(); i++) if (times.get(i).equals(t)) spTime.setSelection(i);
        }
    }

    private void updateSpinner(Spinner spinner, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setSpinnerSelection(Spinner spinner, List<String> ids, String targetId) {
        if (targetId == null) return;
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i).equalsIgnoreCase(targetId)) { spinner.setSelection(i); break; }
        }
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                etDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void showSuccessDialog(boolean isUpdate) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMsg = dialog.findViewById(R.id.tvMessage);
        if (tvMsg != null) {
            tvMsg.setText(isUpdate ? "Cập nhật chuyến xe thành công" : "Tạo chuyến xe thành công");
        }

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        
        // Logic điều hướng khi đóng popup
        dialog.setOnDismissListener(d -> {
            if (isUpdate) {
                // Nếu là Cập nhật -> Quay về màn hình Chi tiết chuyến xe (màn hình trước đó)
                setResult(RESULT_OK);
                finish();
            } else {
                // Nếu là Tạo mới -> Quay về màn hình Danh sách chuyến xe
                Intent intent = new Intent(CreateTripActivity.this, TripListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        // Ấn vào bất kỳ đâu trên popup cũng sẽ thực hiện đóng và chuyển màn hình
        View dialogView = dialog.findViewById(android.R.id.content);
        if (dialogView != null) {
            dialogView.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();

        // Tự động đóng sau 2.5 giây
        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 2500);
    }

    private void showConfirmCancelDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_cancel);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMsg = dialog.findViewById(R.id.tvDialogMessage);
        Button btnNo = dialog.findViewById(R.id.btnNo);
        Button btnYes = dialog.findViewById(R.id.btnYes);

        if (tvMsg != null) {
            tvMsg.setText("Bạn có thông tin chỉnh sửa chưa lưu,\nxác nhận hủy?");
        }

        if (btnNo != null) btnNo.setOnClickListener(v -> dialog.dismiss());
        if (btnYes != null) btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}
