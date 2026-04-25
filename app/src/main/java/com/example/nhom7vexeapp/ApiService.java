package com.example.nhom7vexeapp;

import com.example.nhom7vexeapp.models.LoginRequest;
import com.example.nhom7vexeapp.models.Trip;
import com.example.nhom7vexeapp.models.TripSearchResult;
import com.example.nhom7vexeapp.models.UserResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // Đã cập nhật BASE_URL sang link Render công khai
    String BASE_URL = "https://api-vexeapp.onrender.com/api/";

    @POST("user-auth/")
    Call<User> login(@Body LoginRequest loginRequest);

    @GET("chuyenxe/")
    Call<List<TripSearchResult>> getChuyenXe();

    @GET("user-auth/")
    Call<UserResponse> getAllUsers();
}
