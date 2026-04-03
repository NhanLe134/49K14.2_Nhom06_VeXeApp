package com.example.nhom7vexeapp.api;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    // Thử sử dụng 'username' và 'password' - Đây là chuẩn mặc định của hầu hết API Django
    @SerializedName("username")
    private String username;
    
    @SerializedName("password")
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
