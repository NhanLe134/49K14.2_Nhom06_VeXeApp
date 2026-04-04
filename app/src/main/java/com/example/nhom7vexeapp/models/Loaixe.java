package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;

public class Loaixe {
    @SerializedName("LoaixeID")
    private String loaixeID;

    @SerializedName("NgayCapNhatGia")
    private String ngayCapNhatGia;

    @SerializedName("SoCho")
    private int soCho;

    @SerializedName("SoDoGheNgoiURL")
    private String soDoGheNgoiURL;

    @SerializedName("GiaVe")
    private String giaVe; // Chuyển thành String để khớp với JSON của bạn

    public Loaixe() {}

    public Loaixe(String loaixeID, String ngayCapNhatGia, int soCho, String soDoGheNgoiURL, String giaVe) {
        this.loaixeID = loaixeID;
        this.ngayCapNhatGia = ngayCapNhatGia;
        this.soCho = soCho;
        this.soDoGheNgoiURL = soDoGheNgoiURL;
        this.giaVe = giaVe;
    }

    public String getLoaixeID() { return loaixeID; }
    public void setLoaixeID(String loaixeID) { this.loaixeID = loaixeID; }

    public String getNgayCapNhatGia() { return ngayCapNhatGia; }
    public void setNgayCapNhatGia(String ngayCapNhatGia) { this.ngayCapNhatGia = ngayCapNhatGia; }

    public int getSoCho() { return soCho; }
    public void setSoCho(int soCho) { this.soCho = soCho; }

    public String getSoDoGheNgoiURL() { return soDoGheNgoiURL; }
    public void setSoDoGheNgoiURL(String soDoGheNgoiURL) { this.soDoGheNgoiURL = soDoGheNgoiURL; }

    public String getGiaVe() { return giaVe; }
    public void setGiaVe(String giaVe) { this.giaVe = giaVe; }

    // Hàm tiện ích để lấy giá trị số từ chuỗi
    public double getGiaVeDouble() {
        try {
            return Double.parseDouble(giaVe);
        } catch (Exception e) {
            return 0;
        }
    }
}
