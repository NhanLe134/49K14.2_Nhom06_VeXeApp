package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;

public class ChiTietTaiXeModel {
    @SerializedName("id")
    private Integer id;
    
    @SerializedName("HoTen")
    private String hoTen;
    
    @SerializedName("Tennhaxe")
    private String tennhaxe;
    
    @SerializedName("NgayBatDau")
    private String ngayBatDau;
    
    @SerializedName("NgayKetThuc")
    private String ngayKetThuc;
    
    @SerializedName("Nhaxe")
    private String nhaxe;
    
    @SerializedName("Taixe")
    private String taixe;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getTennhaxe() { return tennhaxe; }
    public void setTennhaxe(String tennhaxe) { this.tennhaxe = tennhaxe; }
    public String getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(String ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public String getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(String ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public String getNhaxe() { return nhaxe; }
    public void setNhaxe(String nhaxe) { this.nhaxe = nhaxe; }
    public String getTaixe() { return taixe; }
    public void setTaixe(String taixe) { this.taixe = taixe; }
}
