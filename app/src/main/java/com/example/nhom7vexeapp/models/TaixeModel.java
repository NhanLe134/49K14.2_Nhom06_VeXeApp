package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;

public class TaixeModel {
    @SerializedName("TaixeID")
    private String taixeID;
    
    @SerializedName("HinhAnhURL")
    private String hinhAnhURL;
    
    @SerializedName("SoBangLai")
    private String soBangLai;
    
    @SerializedName("soCCCD") // Khớp chính xác với models.py
    private String soCCCD;
    
    @SerializedName("LoaiBangLai")
    private String loaiBangLai;
    
    @SerializedName("NgayHetHanBangLai")
    private String ngayHetHanBangLai;

    public String getTaixeID() { return taixeID; }
    public void setTaixeID(String taixeID) { this.taixeID = taixeID; }
    public String getHinhAnhURL() { return hinhAnhURL; }
    public void setHinhAnhURL(String hinhAnhURL) { this.hinhAnhURL = hinhAnhURL; }
    public String getSoBangLai() { return soBangLai; }
    public void setSoBangLai(String soBangLai) { this.soBangLai = soBangLai; }
    public String getSoCCCD() { return soCCCD; }
    public void setSoCCCD(String soCCCD) { this.soCCCD = soCCCD; }
    public String getLoaiBangLai() { return loaiBangLai; }
    public void setLoaiBangLai(String loaiBangLai) { this.loaiBangLai = loaiBangLai; }
    public String getNgayHetHanBangLai() { return ngayHetHanBangLai; }
    public void setNgayHetHanBangLai(String ngayHetHanBangLai) { this.ngayHetHanBangLai = ngayHetHanBangLai; }
}
