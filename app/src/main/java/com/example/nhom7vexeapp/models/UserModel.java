package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    @SerializedName("UserID")
    private String userID;
    @SerializedName("TenDangNhap")
    private String tenDangNhap;
    @SerializedName("MatKhau")
    private String matKhau;
    @SerializedName("Vaitro")
    private String vaitro;
    @SerializedName("SoDienThoai")
    private String soDienThoai;
    @SerializedName("KhachHang")
    private String khachHang;
    @SerializedName("Nhaxe")
    private String nhaxe;
    
    // Thêm trường này để nhận liên kết từ backend
    @SerializedName("Taixe")
    private String taixe;

    @SerializedName("Hovaten")
    private String hovaten;

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getVaitro() { return vaitro; }
    public void setVaitro(String vaitro) { this.vaitro = vaitro; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getKhachHang() { return khachHang; }
    public void setKhachHang(String khachHang) { this.khachHang = khachHang; }
    public String getNhaxe() { return nhaxe; }
    public void setNhaxe(String nhaxe) { this.nhaxe = nhaxe; }
    
    public String getTaixe() { return taixe; }
    public void setTaixe(String taixe) { this.taixe = taixe; }

    public String getHovaten() { return hovaten; }
    public void setHovaten(String hovaten) { this.hovaten = hovaten; }
}
