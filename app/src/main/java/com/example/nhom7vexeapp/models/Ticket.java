package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Ticket implements Serializable {
    @SerializedName("VeID")
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
    private List<String> danhSachGhe;

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
    public List<String> getDanhSachGhe() { return danhSachGhe; }
    public double getTongTien() { return tongTien; }
    public String getTrangThai() { return trangThai; }
    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public String getKhachHangID() { return khachHangID; }
    public String getChuyenXeID() { return chuyenXeID; }

    public String getOwnerId() {
        // Trả về ID khách hàng, đảm bảo không null và đã trim
        return (khachHangID != null) ? khachHangID.trim() : "";
    }

    public String getFormattedSeats() {
        if (danhSachGhe == null || danhSachGhe.isEmpty()) return "N/A";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < danhSachGhe.size(); i++) {
            sb.append(danhSachGhe.get(i));
            if (i < danhSachGhe.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
