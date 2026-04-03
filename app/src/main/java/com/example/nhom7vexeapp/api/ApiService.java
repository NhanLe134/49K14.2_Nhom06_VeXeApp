package com.example.nhom7vexeapp.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import java.util.List;

public interface ApiService {
    @POST("api/user-auth/")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // Lấy danh sách để kiểm tra vai trò và đăng nhập
    @GET("api/user-auth/")
    Call<List<CustomerResponse>> getUsers();
}
