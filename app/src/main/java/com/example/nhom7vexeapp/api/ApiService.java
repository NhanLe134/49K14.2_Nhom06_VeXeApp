package com.example.nhom7vexeapp.api;

import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.Trip;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.List;
import java.util.Map;

public interface ApiService {
    @POST("api/user-auth/")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("api/user-auth/")
    Call<List<CustomerResponse>> getUsers();

    @GET("api/user-auth/{id}/")
    Call<CustomerResponse> getUserAuthDetail(@Path("id") String id);

    @GET("api/khachhang/")
    Call<List<Map<String, Object>>> getKhachHangList();

    @GET("api/chuyenxe/")
    Call<List<Trip>> getTrips();

    @POST("api/chuyenxe/")
    Call<Trip> createTrip(@Body Trip trip);

    @POST("api/chuyenxe/")
    Call<Void> createTripRaw(@Body Map<String, Object> data);

    @PUT("api/chuyenxe/{id}/")
    Call<Void> updateTrip(@Path("id") String id, @Body Trip trip);

    @PUT("api/chuyenxe/{id}/")
    Call<Void> updateTripRaw(@Path("id") String id, @Body Map<String, Object> data);

    @PATCH("api/chuyenxe/{id}/")
    Call<Void> patchTrip(@Path("id") String id, @Body Map<String, Object> data);

    @GET("api/taixe/")
    Call<List<Map<String, Object>>> getDriversRaw();

    @GET("api/chitiettaixe/")
    Call<List<Map<String, Object>>> getChiTietTaiXe();

    @GET("api/ve/")
    Call<List<Map<String, Object>>> getTicketsByTrip(@Query("ChuyenXe") String tripId);

    @PATCH("api/ve/{id}/")
    Call<Void> patchTicket(@Path("id") String id, @Body Map<String, Object> data);

    @GET("api/nhaxe/{id}/")
    Call<Map<String, Object>> getNhaXeDetail(@Path("id") String id);

    @PUT("api/nhaxe/{id}/")
    Call<Void> updateNhaXeProfile(@Path("id") String id, @Body Map<String, String> data);

    @PATCH("api/nhaxe/{id}/")
    Call<Void> patchNhaXeProfile(@Path("id") String id, @Body Map<String, String> data);

    @POST("api/nhaxe/")
    Call<Void> createNhaXeProfile(@Body Map<String, String> data);

    @POST("api/khachhang/")
    Call<Void> createKhachHangProfile(@Body Map<String, String> data);

    @GET("api/khachhang/{id}/")
    Call<Map<String, Object>> getKhachHangDetail(@Path("id") String id);

    @PUT("api/khachhang/{id}/")
    Call<Void> updateKhachHangProfile(@Path("id") String id, @Body Map<String, String> data);

    @GET("api/xe/")
    Call<List<Map<String, Object>>> getVehicles();

    @GET("api/tuyenxe/")
    Call<List<Map<String, Object>>> getRoutes();

    @GET("api/loaixe/")
    Call<List<Loaixe>> getLoaixe();

    @PUT("api/loaixe/{id}/")
    Call<Loaixe> updateLoaixe(@Path("id") String id, @Body Loaixe loaixe);

    @POST("api/ghengoi/")
    Call<Void> createGheNgoi(@Body Map<String, Object> data);

    @POST("api/user-auth/")
    Call<Void> registerAuth(@Body Map<String, String> data);
}
