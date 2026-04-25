package com.example.nhom7vexeapp.repositories;

import com.example.nhom7vexeapp.models.KhachHang;
import com.example.nhom7vexeapp.network.ApiService;
import com.example.nhom7vexeapp.network.RetrofitClient;
import java.util.Map;
import retrofit2.Call;

public class CustomerRepository {
    private ApiService apiService;

    public CustomerRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public Call<KhachHang> register(Map<String, String> body) {
        return apiService.register(body);
    }

    public Call<KhachHang> login(Map<String, String> body) {
        return apiService.login(body);
    }

    public Call<KhachHang> getProfile(String id) {
        return apiService.getProfile(id);
    }

    public Call<KhachHang> updateProfile(String id, KhachHang khachHang) {
        return apiService.updateProfile(id, khachHang);
    }
}
