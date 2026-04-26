package com.example.nhom7vexeapp.api;

import com.example.nhom7vexeapp.models.BookingRequest;
import com.example.nhom7vexeapp.models.KhachHang;
import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.Seat;
import com.example.nhom7vexeapp.models.Trip;
import com.example.nhom7vexeapp.models.TripSearchResult;
import com.example.nhom7vexeapp.api.CustomerResponse;
import com.example.nhom7vexeapp.api.LoginResponse;
import com.example.nhom7vexeapp.TicketModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import java.util.Map;

public interface ApiService {

    // --- 1. AUTH & LOGIN ---
    @POST("api/user-auth/")
    Call<LoginResponse> login(@Body com.example.nhom7vexeapp.models.LoginRequest loginRequest);

    @GET("api/user-auth/")
    Call<List<CustomerResponse>> getUsers();

    @GET("api/user-auth/{id}/")
    Call<CustomerResponse> getUserAuthDetail(@Path("id") String id);

    @POST("api/user-auth/")
    Call<Void> registerAuth(@Body Map<String, String> data);

    // --- 2. QUẢN LÝ CHUYẾN XE ---
    @GET("api/chuyenxe/")
    Call<List<TripSearchResult>> getChuyenXe();

    @GET("api/chuyenxe/")
    Call<List<Trip>> getTrips();

    @POST("api/chuyenxe/")
    Call<Trip> createTrip(@Body Trip trip);

    // --- 3. ĐẶT VÉ & GHẾ NGỒI ---
    @GET("api/ghengoi/")
    Call<List<Seat>> getSeatsByTrip(@Query("ChuyenXe") String chuyenXeId);

    @POST("api/dat-ve/")
    Call<Void> bookTicket(@Body BookingRequest bookingRequest);

    // --- 4. QUẢN LÝ KHÁCH HÀNG ---
    @POST("api/khachhang/")
    Call<Void> createKhachHangProfile(@Body Map<String, String> data);

    @GET("api/khachhang/{id}/")
    Call<Map<String, Object>> getKhachHangDetail(@Path("id") String id);

    @PUT("api/khachhang/{id}/")
    Call<Void> updateKhachHang(@Path("id") String id, @Body Map<String, String> data);

    @PATCH("api/khachhang/{id}/")
    Call<Void> patchKhachHang(@Path("id") String id, @Body Map<String, Object> data);

    // --- 5. QUẢN LÝ NHÀ XE ---
    @GET("api/nhaxe/{id}/")
    Call<Map<String, Object>> getNhaXeDetail(@Path("id") String id);

    @PATCH("api/nhaxe/{id}/")
    Call<Void> patchNhaXeProfile(@Path("id") String id, @Body Map<String, Object> data);

    // --- 6. QUẢN LÝ LOẠI XE ---
    @GET("api/loaixe/")
    Call<List<Loaixe>> getLoaixe();

    @PUT("api/loaixe/{id}/")
    Call<Loaixe> updateLoaixe(@Path("id") String id, @Body Loaixe loaixe);
}
