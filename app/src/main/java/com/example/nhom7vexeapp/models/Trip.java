package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trip implements Serializable {
    @SerializedName("ChuyenXeID")
    private String id;

    @SerializedName("NgayKhoiHanh")
    private String date;

    @SerializedName("GioDi")
    private String startTime;

    @SerializedName("GioDen")
    private String endTime;

    @SerializedName("TrangThai")
    private String status;

    @SerializedName("Xe")
    private String xe; // Dùng chung cho xeID

    @SerializedName("TuyenXe")
    private String tuyen; // Dùng chung cho tuyenXeID

    @SerializedName("Taixe")
    private String taiXeID;

    @SerializedName("TenTuyen")
    private String tenTuyen;

    @SerializedName("LoaiXe")
    private String loaiXe;

    @SerializedName("GiaVe")
    private String giaVe;

    @SerializedName("TenNhaXe")
    private String tenNhaXe;

    @SerializedName("SoChoTrong")
    private int soChoTrong;

    @SerializedName("ThoiGian")
    private String duration;

    // --- Các trường hỗ trợ logic quản lý nội bộ (Merge từ File 1) ---
    private int seats = 4; // Giá trị mặc định từ File 1
    private Driver assignedDriver;
    private List<Passenger> passengers = new ArrayList<>();

    // No-arg constructor cho GSON
    public Trip() {
    }

    public Trip(String id, String tuyen, String date, String startTime, String status) {
        this.id = id;
        this.tuyen = tuyen;
        this.date = date;
        this.startTime = startTime;
        this.status = status;
    }

    // --- Getters & Setters ---

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date != null ? date : ""; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return startTime != null ? startTime : "00:00"; }
    public void setTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime != null ? endTime : (startTime != null ? startTime : ""); }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status != null ? status : "Mới"; }
    public void setStatus(String status) { this.status = status; }

    public String getXeID() { return xe != null ? xe : ""; }
    public void setXeID(String xe) { this.xe = xe; }

    public String getTuyenXeID() { return tuyen != null ? tuyen : ""; }
    public void setTuyenXeID(String tuyen) { this.tuyen = tuyen; }

    public String getTaiXeID() { return taiXeID != null ? taiXeID : ""; }
    public void setTaiXeID(String taiXeID) { this.taiXeID = taiXeID; }

    public String getDuration() { return duration != null ? duration : "2h"; }
    public void setDuration(String duration) { this.duration = duration; }

    // --- Logic hiển thị thông minh (Kết hợp cả 2 file) ---
    public String getRouteName() {
        return (tenTuyen != null && !tenTuyen.isEmpty()) ? tenTuyen : "Tuyến: " + getTuyenXeID();
    }

    public String getVehicleType() {
        return (loaiXe != null && !loaiXe.isEmpty()) ? loaiXe : "Xe: " + getXeID();
    }

    public String getGiaVe() { return giaVe != null ? giaVe : "0"; }

    // Ưu tiên soChoTrong từ API, nếu không có thì dùng biến seats local
    public int getSeats() { return soChoTrong > 0 ? soChoTrong : seats; }
    public void setSeats(int seats) {
        this.seats = seats;
        this.soChoTrong = seats;
    }

    public List<Passenger> getPassengers() {
        if (passengers == null) passengers = new ArrayList<>();
        return passengers;
    }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }

    public Driver getAssignedDriver() { return assignedDriver; }
    public void setAssignedDriver(Driver driver) { this.assignedDriver = driver; }

    public String getTenNhaXe() { return tenNhaXe != null ? tenNhaXe : ""; }
    public void setTenNhaXe(String tenNhaXe) { this.tenNhaXe = tenNhaXe; }
}