package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class KhachHang implements Serializable {
    @SerializedName(value = "KhachHangID", alternate = {"id", "khach_hang_id", "MaKH"})
    private String khachHangID;
    
    @SerializedName(value = "Hovaten", alternate = {"ho_ten", "TenKhachHang", "HoTen", "name", "full_name", "ten_khach_hang"})
    private String hoTen;
    
    @SerializedName("Email")
    private String email;
    
    @SerializedName(value = "Ngaysinh", alternate = {"ngay_sinh", "dob", "NgaySinh", "ngay_sinh_kh"})
    private String ngaySinh;
    
    @SerializedName(value = "AnhDaiDienURL", alternate = {"anh_dai_dien", "avatar", "image", "hinh_anh", "AnhDaiDien", "anh_dai_dien_url"})
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

    public String getHoTen() { 
        return (hoTen != null && !hoTen.trim().isEmpty()) ? hoTen : "Khách hàng"; 
    }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getAnhDaiDienURL() { return anhDaiDienURL; }
    public void setAnhDaiDienURL(String anhDaiDienURL) { this.anhDaiDienURL = anhDaiDienURL; }

    public String getNgayDangKy() { return ngayDangKy; }
}
