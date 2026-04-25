package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class VehicleDomain implements Serializable {
    @SerializedName("XeID")
    private String xeID;

    @SerializedName("BienSo")
    private String bienSo;

    @SerializedName("TrangThai")
    private String trangThai; // Đang hoạt động, Bảo trì, Tạm dừng

    @SerializedName("LoaixeID")
    private String loaiXeID;

    @SerializedName("NhaxeID")
    private String nhaXeID;

    // Thông tin lồng (Nested object) từ API nếu có
    @SerializedName("LoaiXeDetail")
    private Loaixe loaiXeDetail;

    public VehicleDomain() {}

    // Getters and Setters
    public String getXeID() { return xeID; }
    public void setXeID(String xeID) { this.xeID = xeID; }
    public String getBienSo() { return bienSo; }
    public void setBienSo(String bienSo) { this.bienSo = bienSo; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getLoaiXeID() { return loaiXeID; }
    public void setLoaiXeID(String loaiXeID) { this.loaiXeID = loaiXeID; }
    public String getNhaXeID() { return nhaXeID; }
    public void setNhaXeID(String nhaXeID) { this.nhaXeID = nhaXeID; }
    public Loaixe getLoaiXeDetail() { return loaiXeDetail; }
    public void setLoaiXeDetail(Loaixe loaiXeDetail) { this.loaiXeDetail = loaiXeDetail; }
}
