package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookingRequest {
    @SerializedName("ve_id")
    private String veId;

    @SerializedName("chuyen_xe")
    private String chuyenXe;
    
    @SerializedName("khach_hang")
    private String khachHang; 
    
    @SerializedName("danh_sach_ghe")
    private List<String> danhSachGhe; 
    
    @SerializedName("tong_tien")
    private long tongTien;
    
    @SerializedName("diem_don")
    private String diemDon;
    
    @SerializedName("diem_tra")
    private String diemTra;

    public BookingRequest(String veId, String chuyenXe, String khachHang, List<String> danhSachGhe, long tongTien, 
                          String diemDon, String diemTra) {
        this.veId = veId;
        this.chuyenXe = chuyenXe;
        this.khachHang = khachHang;
        this.danhSachGhe = danhSachGhe;
        this.tongTien = tongTien;
        this.diemDon = diemDon;
        this.diemTra = diemTra;
    }
}
