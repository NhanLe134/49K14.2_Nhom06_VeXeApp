package com.example.nhom7vexeapp.api;

import com.google.gson.annotations.SerializedName;

public class CustomerResponse {
    @SerializedName(value = "UserID", alternate = {"USERID", "userid", "user_id"})
    private String UserID;

    @SerializedName(value = "TenDangNhap", alternate = {"TENDANGNHAP", "tendangnhap"})
    private String TenDangNhap;
    
    @SerializedName(value = "Vaitro", alternate = {"VAITRO", "vaitro", "vai_tro"})
    private String Vaitro;
    
    @SerializedName(value = "SoDienThoai", alternate = {"SODIENTHOAI", "sodienthoai", "sdt"})
    private String SoDienThoai;

    @SerializedName(value = "MatKhau", alternate = {"MATKHAU", "matkhau"})
    private String MatKhau;

    // Các getter cần thiết
    public String getUserID() { return UserID; }
    public String getVaitro() { return Vaitro; }
    public String getSdt() { return SoDienThoai; }
    public String getTenKhachHang() { return TenDangNhap; }
    public String getMatKhau() { return MatKhau; }
    public String getNgaySinh() { return "Chưa cập nhật"; }
}
