package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Trip implements Serializable {
    @SerializedName("ChuyenXeID")
    private String id;

    @SerializedName("Xe")
    private String xeID;

    @SerializedName("TuyenXe")
    private String tuyenXeID;

    @SerializedName("NgayKhoiHanh")
    private String date;

    @SerializedName("GioDi")
    private String startTime;

    @SerializedName("TrangThai")
    private String status;

    // Các trường phục vụ hiển thị trên App
    private int seats = 4; // Mặc định 4 chỗ nếu server chưa trả về
    private Driver assignedDriver;
    private List<Passenger> passengers = new ArrayList<>();

    public Trip(String id, String tuyenXeID, String date, String startTime, String status) {
        this.id = id;
        this.tuyenXeID = tuyenXeID;
        this.date = date;
        this.startTime = startTime;
        this.status = status;
    }

    public String getId() { return id; }
    public String getDate() { return date; }
    public String getTime() { return startTime; }
    public String getStatus() { return status; }
    public String getRouteName() { return "Tuyến: " + tuyenXeID; }
    
    // Các phương thức mà TripDetailActivity đang yêu cầu
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    
    public List<Passenger> getPassengers() { return passengers; }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }
    
    public Driver getAssignedDriver() { return assignedDriver; }
    public void setAssignedDriver(Driver assignedDriver) { this.assignedDriver = assignedDriver; }

    public String getVehicleType() { return "Mã xe: " + (xeID != null ? xeID : "Chưa gán"); }
}
