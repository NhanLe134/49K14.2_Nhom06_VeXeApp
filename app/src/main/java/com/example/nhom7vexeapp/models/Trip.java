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
    private String xe; 

    @SerializedName("TuyenXe")
    private String tuyen; 

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
    private Integer soChoTrong; // Dùng Integer để phân biệt null (chưa có dữ liệu) và 0 (hết chỗ)

    @SerializedName("ThoiGian")
    private String duration;

    private int seats = 4; // Giá trị mặc định
    private Driver assignedDriver;
    private List<Passenger> passengers = new ArrayList<>();

    public Trip() {
    }

    public Trip(String id, String tuyen, String date, String startTime, String status) {
        this.id = id;
        this.tuyen = tuyen;
        this.date = date;
        this.startTime = startTime;
        this.status = status;
    }

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

    public String getRouteName() {
        return (tenTuyen != null && !tenTuyen.isEmpty()) ? tenTuyen : "Tuyến: " + getTuyenXeID();
    }

    public String getVehicleType() {
        return (loaiXe != null && !loaiXe.isEmpty()) ? loaiXe : "Xe: " + getXeID();
    }

    public String getGiaVe() { return giaVe != null ? giaVe : "0"; }

    /**
     * Logic lấy số chỗ trống:
     * 1. Ưu tiên giá trị soChoTrong từ API (kể cả khi bằng 0 - tức là hết chỗ).
     * 2. Nếu soChoTrong là null (API không trả về), tự suy luận từ loaiXe hoặc dùng mặc định.
     */
    public int getSeats() { 
        if (soChoTrong != null) {
            return soChoTrong; 
        }
        
        // Fallback logic: Suy luận sức chứa từ tên loại xe nếu server không gửi số chỗ trống
        String type = getVehicleType().toLowerCase();
        if (type.contains("16")) return 16;
        if (type.contains("9")) return 9;
        if (type.contains("7")) return 7;
        
        return seats; 
    }

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

    public String getLoaiXe() { return loaiXe; }
    public void setLoaiXe(String loaiXe) { this.loaiXe = loaiXe; }
}