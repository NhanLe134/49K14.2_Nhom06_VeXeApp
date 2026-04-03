package com.example.nhom7vexeapp.api;

import com.google.gson.annotations.SerializedName;

public class CustomerResponse {
    @SerializedName(value = "TENDANGNHAP", alternate = {"TenDangNhap", "tendangnhap", "ten_khach_hang"})
    private String TenDangNhap;
    
    @SerializedName(value = "VAITRO", alternate = {"Vaitro", "vaitro", "vai_tro"})
    private String Vaitro;
    
    @SerializedName(value = "SODIENTHOAI", alternate = {"SoDienThoai", "sodienthoai", "sdt"})
    private String SoDienThoai;

    @SerializedName(value = "MATKHAU", alternate = {"MatKhau", "matkhau"})
    private String MatKhau;

    public String getVaitro() { return Vaitro; }
    public String getSdt() { return SoDienThoai; }
    public String getTenKhachHang() { return TenDangNhap; }
    public String getMatKhau() { return MatKhau; }
    public String getNgaySinh() { return "Chưa cập nhật"; }
}
