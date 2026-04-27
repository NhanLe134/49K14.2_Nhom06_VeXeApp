package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;

public class Seat {
    @SerializedName("gheID")
    private String id;
    
    @SerializedName("soGhe")
    private String seatCode;
    
    @SerializedName("trangThai")
    private String status;

    @SerializedName("ChuyenXe")
    private String chuyenXe; // Thêm trường này để lọc đúng chuyến xe

    @SerializedName("VeID")
    private String ticketId; // Thêm trường này để ánh xạ với Vé

    public String getId() { return id; }
    public String getSeatCode() { return seatCode; }
    public String getStatus() { return status; }
    public String getChuyenXe() { return chuyenXe; }
    public String getTicketId() { return ticketId; }
    
    public void setStatus(String status) { this.status = status; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
}
