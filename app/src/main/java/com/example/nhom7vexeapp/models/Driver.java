package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Driver implements Serializable {
    @SerializedName(value = "TaiXeID", alternate = {"taixeid", "id"})
    private String id;
    
    @SerializedName(value = "HoTen", alternate = {"hoten", "name", "HoTenTaiXe"})
    private String name;
    
    @SerializedName(value = "SoDienThoai", alternate = {"sodienthoai", "phone"})
    private String phone;

    @SerializedName(value = "NhaXe", alternate = {"nhaxe"})
    private String nhaXe;

    public Driver(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() { return id != null ? id : ""; }
    public String getName() { return name != null ? name : "N/A"; }
    public String getPhone() { return phone != null ? phone : ""; }
    public String getNhaXe() { return nhaXe != null ? nhaXe : ""; }
}
