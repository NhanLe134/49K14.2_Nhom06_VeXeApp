package com.example.nhom7vexeapp.models;

import java.io.Serializable;

public class Driver implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String avatarUrl;
    private String pickup;
    private String dropoff;

    public Driver(String id, String name, String phone, String avatarUrl, String pickup, String dropoff) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.pickup = pickup;
        this.dropoff = dropoff;
    }

    public Driver(String id, String name, String phone, String avatarUrl) {
        this(id, name, phone, avatarUrl, "Chưa cập nhật", "Chưa cập nhật");
    }

    public Driver(String id, String name, String phone) {
        this(id, name, phone, "", "Chưa cập nhật", "Chưa cập nhật");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPickup() { return pickup; }
    public String getDropoff() { return dropoff; }
}
