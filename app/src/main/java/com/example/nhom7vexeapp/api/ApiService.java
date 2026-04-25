package com.example.nhom7vexeapp.api;

import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.Trip;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import java.util.Map;

public interface ApiService {
    @POST("api/user-auth/") 
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("api/user-auth/")
    Call<List<CustomerResponse>> getUsers();

    @GET("api/user-auth/{id}/")
    Call<CustomerResponse> getUserAuthDetail(@Path("id") String id);

    @POST("api/user-auth/")
    Call<Void> registerAuth(@Body Map<String, String> data);

    // QUẢN LÝ NHÀ XE (Code của đồng đội)
    @POST("api/nhaxe/")
    Call<Void> createNhaXeProfile(@Body Map<String, String> data);

    @GET("api/nhaxe/{id}/")
    Call<Map<String, Object>> getNhaXeDetail(@Path("id") String id);

    @PUT("api/nhaxe/{id}/")
    Call<Void> updateNhaXeProfile(@Path("id") String id, @Body Map<String, String> data);

    // QUẢN LÝ KHÁCH HÀNG (Tính năng mới)
    @POST("api/khachhang/")
    Call<Void> createKhachHangProfile(@Body Map<String, String> data);

    @GET("api/khachhang/{id}/")
    Call<Map<String, Object>> getKhachHangDetail(@Path("id") String id);

    @DELETE("api/khachhang/{id}/")
    Call<Void> deleteKhachHang(@Path("id") String id);

    @PUT("api/khachhang/{id}/")
    Call<Void> updateKhachHang(@Path("id") String id, @Body Map<String, String> data);

    // QUẢN LÝ CHUYẾN XE (Code của đồng đội)
    @GET("api/chuyenxe/")
    Call<List<Trip>> getTrips();

    @POST("api/chuyenxe/")
    Call<Trip> createTrip(@Body Trip trip);

    // QUẢN LÝ LOẠI XE (Code của đồng đội)
    @GET("api/loaixe/")
    Call<List<Loaixe>> getLoaixe();

    @PUT("api/loaixe/{id}/")
    Call<Loaixe> updateLoaixe(@Path("id") String id, @Body Loaixe loaixe);
}
