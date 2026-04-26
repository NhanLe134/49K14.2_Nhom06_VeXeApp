package com.example.nhom7vexeapp.models;

import java.io.Serializable;

public class Driver implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String avatarUrl;

    public Driver(String id, String name, String phone, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
}
