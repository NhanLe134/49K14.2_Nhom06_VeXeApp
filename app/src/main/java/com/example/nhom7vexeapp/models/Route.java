package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Route implements Serializable {
    @SerializedName("tuyenXeID")
    private String id;

    @SerializedName("tenTuyen")
    private String name;

    @SerializedName("diemDi")
    private String startPoint;

    @SerializedName("DiemTrungGian")
    private String midPoint;

    @SerializedName("diemDen")
    private String endPoint;

    @SerializedName("QuangDuong")
    private String distance;

    @SerializedName("ThoiGian")
    private String time;

    @SerializedName("TrangThai")
    private String status; // "Đang hoạt động", "Bảo trì" hoặc "Ngưng hoạt động"

    @SerializedName("nhaXe")
    private String nhaXeId;

    public Route(String id, String name, String startPoint, String midPoint, String endPoint, String distance, String time, String status) {
        this.id = id;
        this.name = name;
        this.startPoint = startPoint;
        this.midPoint = midPoint;
        this.endPoint = endPoint;
        this.distance = distance;
        this.time = time;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStartPoint() { return startPoint; }
    public void setStartPoint(String startPoint) { this.startPoint = startPoint; }
    public String getMidPoint() { return midPoint; }
    public void setMidPoint(String midPoint) { this.midPoint = midPoint; }
    public String getEndPoint() { return endPoint; }
    public void setEndPoint(String endPoint) { this.endPoint = endPoint; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNhaXeId() { return nhaXeId; }
    public void setNhaXeId(String nhaXeId) { this.nhaXeId = nhaXeId; }
}

