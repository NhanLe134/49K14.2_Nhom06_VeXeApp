package com.example.nhom7vexeapp.api;

import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.Trip;
import com.example.nhom7vexeapp.models.TaixeModel;
import com.example.nhom7vexeapp.models.ChiTietTaiXeModel;
import com.example.nhom7vexeapp.models.UserModel;
import com.example.nhom7vexeapp.models.NhaXe;
import com.example.nhom7vexeapp.models.Route;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- USER AUTH ---
    @POST("api/user-auth/")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("api/user-auth/")
    Call<List<UserModel>> getUsers(@Query("Method") String method);

    @GET("api/user-auth/{id}/")
    Call<CustomerResponse> getUserAuthDetail(@Path("id") String id);

    @POST("api/user-auth/")
    Call<UserModel> createUser(@Query("Method") String method, @Body UserModel user);

    @PUT("api/user-auth/{id}/")
    Call<UserModel> updateUser(@Path("id") String id, @Body UserModel user);

    @DELETE("api/user-auth/{id}/")
    Call<Void> deleteUser(@Path("id") String id);

    @POST("api/user-auth/")
    Call<Void> registerAuth(@Body Map<String, String> data);

    // --- TÀI XẾ ---
    @GET("api/taixe/")
    Call<List<TaixeModel>> getTaixeList(@Query("Method") String method);

    @GET("api/taixe/")
    Call<List<Map<String, Object>>> getDriversRaw();

    @POST("api/taixe/")
    Call<TaixeModel> createTaixe(@Query("Method") String method, @Body TaixeModel taixe);

    @PUT("api/taixe/{id}/")
    Call<TaixeModel> updateTaixe(@Path("id") String id, @Body TaixeModel taixe);

    @DELETE("api/taixe/{id}/")
    Call<Void> deleteTaixe(@Path("id") String id);

    // --- CHI TIẾT TÀI XẾ ---
    @GET("api/chitiettaixe/")
    Call<List<Map<String, Object>>> getChiTietTaiXe();

    @GET("api/chitiettaixe/")
    Call<List<ChiTietTaiXeModel>> getChiTietTaiXeList(@Query("Method") String method);

    @POST("api/chitiettaixe/")
    Call<ChiTietTaiXeModel> createChiTietTaiXe(@Query("Method") String method, @Body ChiTietTaiXeModel chiTiet);

    @PUT("api/chitiettaixe/{id}/")
    Call<ChiTietTaiXeModel> updateChiTietTaiXe(@Path("id") String id, @Body ChiTietTaiXeModel chiTiet);

    // --- QUẢN LÝ NHÀ XE ---
    @POST("api/nhaxe/")
    Call<Void> createNhaXeProfile(@Body Map<String, String> data);

    @GET("api/nhaxe/{id}/")
    Call<NhaXe> getNhaXeDetail(@Path("id") String id);

    @PUT("api/nhaxe/{id}/")
    Call<Void> updateNhaXeProfile(@Path("id") String id, @Body Map<String, String> data);

    @PATCH("api/nhaxe/{id}/")
    Call<Void> patchNhaXeProfile(@Path("id") String id, @Body Map<String, String> data);

    // --- LOẠI XE ---
    @GET("api/loaixe/")
    Call<List<Loaixe>> getLoaixe();

    @PUT("api/loaixe/{id}/")
    Call<Loaixe> updateLoaixe(@Path("id") String id, @Body Loaixe loaixe);

    // --- TUYẾN XE ---
    @GET("api/tuyenxe/")
    Call<List<Map<String, Object>>> getRoutes();

    @GET("api/tuyenxe/")
    Call<List<Route>> getRoutesModel();

    @PUT("api/tuyenxe/{id}/")
    Call<Void> updateRoute(@Path("id") String id, @Body Map<String, String> data);

    // --- CHUYẾN XE ---
    @GET("api/chuyenxe/")
    Call<List<Trip>> getTrips();

    @POST("api/chuyenxe/")
    Call<List<Trip>> createTrip(@Body Trip trip); // từ file 1

    @POST("api/chuyenxe/")
    Call<Trip> createTripSingle(@Body Trip trip); // từ file 2 (đổi tên để phân biệt với List<Trip>)

    @POST("api/chuyenxe/")
    Call<Void> createTripRaw(@Body Map<String, Object> data);

    @PUT("api/chuyenxe/{id}/")
    Call<Void> updateTrip(@Path("id") String id, @Body Trip trip);

    @PUT("api/chuyenxe/{id}/")
    Call<Void> updateTripRaw(@Path("id") String id, @Body Map<String, Object> data);

    @PATCH("api/chuyenxe/{id}/")
    Call<Void> patchTrip(@Path("id") String id, @Body Map<String, Object> data);

    // --- VÉ XE ---
    @GET("api/ve/")
    Call<List<Map<String, Object>>> getTicketsByTrip(@Query("ChuyenXe") String tripId);

    @PATCH("api/ve/{id}/")
    Call<Void> patchTicket(@Path("id") String id, @Body Map<String, Object> data);

    // --- GHẾ NGỒI ---
    @POST("api/ghengoi/")
    Call<Void> createGheNgoi(@Body Map<String, Object> data);

    // --- QUẢN LÝ KHÁCH HÀNG ---
    @GET("api/khachhang/")
    Call<List<Map<String, Object>>> getKhachHangList();

    @Multipart
    @POST("api/khachhang/")
    Call<Void> createKhachHangProfile(
            @Part("KhachHangID") RequestBody id,
            @Part("hoTen") RequestBody name,
            @Part("Email") RequestBody email,
            @Part("Ngaysinh") RequestBody dob,
            @Part MultipartBody.Part imageFile
    );

    @POST("api/khachhang/")
    Call<Void> createKhachHangProfile(@Body Map<String, String> data);

    @GET("api/khachhang/{id}/")
    Call<Map<String, Object>> getKhachHangDetail(@Path("id") String id);

    @PUT("api/khachhang/{id}/")
    Call<Void> updateKhachHangProfile(@Path("id") String id, @Body Map<String, String> data);

    @DELETE("api/khachhang/{id}/")
    Call<Void> deleteKhachHang(@Path("id") String id);

    // --- PHƯƠNG TIỆN (XE) ---
    @GET("api/xe/")
    Call<List<Map<String, Object>>> getVehicles();
}
