package com.example.nhom7vexeapp;

import com.example.nhom7vexeapp.api.ApiService; // Trỏ sang package api
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    
    // BASE_URL giờ lấy từ ApiService trong package api
    private static final String BASE_URL = "https://api-vexeapp.onrender.com/";

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
