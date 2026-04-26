package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;

public class Seat {
    @SerializedName(value = "gheID", alternate = {"GHEID", "ghe_id", "id", "MaGhe"})
    private String id;
    
    @SerializedName(value = "soGhe", alternate = {"SOGHE", "so_ghe", "SoGhe", "code"})
    private String seatCode;
    
    @SerializedName(value = "trangThai", alternate = {"TRANGTHAI", "trang_thai", "status"})
    private String status;

    @SerializedName(value = "ChuyenXe", alternate = {"chuyen_xe", "chuyenxe", "ChuyenXeID"})
    private String chuyenXe;

    // QUAN TRỌNG: Ánh xạ trường Ve từ Django để biết ghế thuộc vé nào
    @SerializedName(value = "Ve", alternate = {"ve", "ticket", "VeID", "ve_id"})
    private String ticketId;

    public String getId() { return id != null ? id : ""; }
    public String getSeatCode() { return seatCode != null ? seatCode : ""; }
    public String getStatus() { return status != null ? status : ""; }
    public String getChuyenXe() { return chuyenXe; }
    public String getTicketId() { return ticketId != null ? ticketId : ""; }
    
    public void setStatus(String status) { this.status = status; }
}
