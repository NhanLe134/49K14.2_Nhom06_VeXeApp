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
    private String xe; // Lưu ID xe

    @SerializedName("TuyenXe")
    private String tuyen; // Lưu ID tuyến xe

    @SerializedName("Taixe")
    private String taiXeID;

    // Các trường bổ sung từ API để hiển thị (Theirs)
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

    // Các trường hỗ trợ quản lý của bạn (Yours)
    private int seats = 4;
    private Driver assignedDriver;
    private List<Passenger> passengers = new ArrayList<>();

    public Trip() {}

    public Trip(String id, String tuyen, String date, String startTime, String status) {
        this.id = id;
        this.tuyen = tuyen;
        this.date = date;
        this.startTime = startTime;
        this.status = status;
    }

    // --- GETTERS & SETTERS (Giữ logic đặt vé) ---
    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date != null ? date : ""; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return startTime != null ? startTime : "00:00"; }
    public void setTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime != null ? endTime : ""; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status != null ? status : "Mới"; }
    public void setStatus(String status) { this.status = status; }

    public String getXeID() { return xe != null ? xe : ""; }
    public void setXeID(String xe) { this.xe = xe; }

    public String getTuyenXeID() { return tuyen != null ? tuyen : ""; }
    public void setTuyenXeID(String tuyen) { this.tuyen = tuyen; }

    public String getTaiXeID() { return taiXeID != null ? taiXeID : ""; }
    public void setTaiXeID(String taiXeID) { this.taiXeID = taiXeID; }

    // Logic hiển thị thông minh: Ưu tiên tên từ API, nếu không có thì dùng ID
    public String getRouteName() {
        return (tenTuyen != null && !tenTuyen.isEmpty()) ? tenTuyen : "Tuyến: " + getTuyenXeID();
    }

    public String getVehicleType() {
        return (loaiXe != null && !loaiXe.isEmpty()) ? loaiXe : "Xe: " + getXeID();
    }

    public String getGiaVe() { return giaVe; }

    // Ưu tiên số chỗ trống từ API (soChoTrong) nếu > 0, ngược lại dùng biến seats
    public int getSeats() { return soChoTrong > 0 ? soChoTrong : seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public String getDuration() { return duration != null ? duration : "2h"; }
    public void setDuration(String duration) { this.duration = duration; }

    public List<Passenger> getPassengers() {
        if (passengers == null) passengers = new ArrayList<>();
        return passengers;
    }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }

    public Driver getAssignedDriver() { return assignedDriver; }
    public void setAssignedDriver(Driver driver) { this.assignedDriver = driver; }
}