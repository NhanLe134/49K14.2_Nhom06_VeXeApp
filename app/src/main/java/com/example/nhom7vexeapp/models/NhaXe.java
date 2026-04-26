package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class NhaXe implements Serializable {
    @SerializedName("NhaxeID")
    private String id;

    @SerializedName("Tennhaxe")
    private String busName;

    @SerializedName("TenNguoiDaiDien")
    private String representative;

    @SerializedName("Email")
    private String email;

    @SerializedName("AnhDaiDien")
    private String bannerUrl; // Đã đổi SerializedName thành AnhDaiDien để khớp với Admin

    @SerializedName("DiaChiTruSo")
    private String address;

    @SerializedName("SoDienThoai")
    private String phone;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRepresentative() { return representative; }
    public void setRepresentative(String representative) { this.representative = representative; }
}
