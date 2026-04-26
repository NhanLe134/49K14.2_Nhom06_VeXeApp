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
    private int soChoTrong;

    // ✅ BỔ SUNG TRƯỜNG THỜI GIAN
    @SerializedName("ThoiGian")
    private String duration;

    private List<Passenger> passengers = new ArrayList<>();

    public Trip() {}

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
    public String getTime() { return startTime != null ? startTime : ""; }
    public void setTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime != null ? endTime : startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getStatus() { return status != null ? status : "Mới"; }
    public void setStatus(String status) { this.status = status; }
    public String getXeID() { return xe; }
    public void setXeID(String xe) { this.xe = xe; }
    public String getTuyenXeID() { return tuyen; }
    public void setTuyenXeID(String tuyen) { this.tuyen = tuyen; }
    public String getTaiXeID() { return taiXeID; }
    public void setTaiXeID(String taiXeID) { this.taiXeID = taiXeID; }
    public String getRouteName() { return (tenTuyen != null && !tenTuyen.isEmpty()) ? tenTuyen : "Tuyến: " + tuyen; }
    public String getVehicleType() { return (loaiXe != null && !loaiXe.isEmpty()) ? loaiXe : "Xe: " + xe; }
    public String getGiaVe() { return giaVe; }
    public int getSeats() { return soChoTrong; }
    public String getDuration() { return duration != null ? duration : "2h"; }
    public void setDuration(String duration) { this.duration = duration; }

    public List<Passenger> getPassengers() {
        if (passengers == null) passengers = new ArrayList<>();
        return passengers;
    }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }
}
