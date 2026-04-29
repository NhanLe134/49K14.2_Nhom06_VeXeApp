package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Ticket implements Serializable {
    @SerializedName(value = "VeID", alternate = {"VeHuyID"})
    private String veID;

    @SerializedName("TenTuyen")
    private String tenTuyen;

    @SerializedName("TenNhaXe")
    private String tenNhaXe;

    @SerializedName("NgayKhoiHanh")
    private String ngayKhoiHanh;

    @SerializedName("GioDi")
    private String gioDi;

    @SerializedName("SoLuongGhe")
    private int soLuongGhe;

    @SerializedName("DanhSachGhe")
    private Object danhSachGhe; // Có thể là List<String> hoặc String từ bảng VeHuy

    @SerializedName("TongTien")
    private double tongTien;

    @SerializedName("TrangThai")
    private String trangThai; // "Đã đặt", "Đã đi", "Đã hủy"

    @SerializedName("TrangThaiThanhToan")
    private String trangThaiThanhToan;

    @SerializedName("KhachHang")
    private String khachHangID;

    @SerializedName("ChuyenXe")
    private String chuyenXeID;

    public String getVeID() { return veID; }
    public String getTenTuyen() { return tenTuyen != null ? tenTuyen : "N/A"; }
    public String getTenNhaXe() { return tenNhaXe != null ? tenNhaXe : "N/A"; }
    public String getNgayKhoiHanh() { return ngayKhoiHanh; }
    public String getGioDi() { return gioDi; }
    public int getSoLuongGhe() { return soLuongGhe; }
    
    public double getTongTien() { return tongTien; }
    public String getTrangThai() { return trangThai; }
    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public String getKhachHangID() { return khachHangID; }
    public String getChuyenXeID() { return chuyenXeID; }

    public void setTenTuyen(String tenTuyen) { this.tenTuyen = tenTuyen; }
    public void setTenNhaXe(String tenNhaXe) { this.tenNhaXe = tenNhaXe; }
    public void setNgayKhoiHanh(String ngayKhoiHanh) { this.ngayKhoiHanh = ngayKhoiHanh; }
    public void setGioDi(String gioDi) { this.gioDi = gioDi; }

    public String getOwnerId() {
        return (khachHangID != null) ? khachHangID.trim() : "";
    }

    public String getFormattedSeats() {
        if (danhSachGhe == null) return "N/A";
        if (danhSachGhe instanceof String) {
            return (String) danhSachGhe;
        }
        if (danhSachGhe instanceof List) {
            List<String> list = (List<String>) danhSachGhe;
            if (list.isEmpty()) return "N/A";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i < list.size() - 1) sb.append(", ");
            }
            return sb.toString();
        }
        return "N/A";
    }

    public List<String> getDanhSachGhe() {
        if (danhSachGhe instanceof List) return (List<String>) danhSachGhe;
        return null;
    }
}
