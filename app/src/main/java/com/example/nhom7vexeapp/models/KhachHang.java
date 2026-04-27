package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class KhachHang implements Serializable {
    @SerializedName("KhachHangID")
    private String khachHangID;
    
    @SerializedName("hoTen")
    private String hoTen;
    
    @SerializedName("Email")
    private String email;
    
    @SerializedName("Ngaysinh")
    private String ngaySinh;
    
    @SerializedName("AnhDaiDienURL")
    private String anhDaiDienURL;
    
    @SerializedName("NgayDangKy")
    private String ngayDangKy;

    public KhachHang() {}

    public KhachHang(String hoTen, String email, String ngaySinh) {
        this.hoTen = hoTen;
        this.email = email;
        this.ngaySinh = ngaySinh;
    }

    public String getKhachHangID() { return khachHangID; }
    public void setKhachHangID(String khachHangID) { this.khachHangID = khachHangID; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getAnhDaiDienURL() { return anhDaiDienURL; }
    public void setAnhDaiDienURL(String anhDaiDienURL) { this.anhDaiDienURL = anhDaiDienURL; }

    public String getNgayDangKy() { return ngayDangKy; }
}
