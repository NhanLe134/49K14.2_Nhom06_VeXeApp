package com.example.nhom7vexeapp.api;

import com.example.nhom7vexeapp.models.BookingRequest;
import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.Trip;
import com.example.nhom7vexeapp.models.Driver;
import com.example.nhom7vexeapp.models.TripSearchResult;
import com.example.nhom7vexeapp.models.VehicleManaged;
import com.example.nhom7vexeapp.models.NhaXe;
import com.example.nhom7vexeapp.models.Route;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import com.example.nhom7vexeapp.models.UserModel;
import com.example.nhom7vexeapp.models.TaixeModel;
import com.example.nhom7vexeapp.models.ChiTietTaiXeModel;
import com.example.nhom7vexeapp.models.LoginRequest;
import com.example.nhom7vexeapp.models.KhachHang;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // --- 1. AUTH & USER ---
    @POST("api/user-auth/")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/khachhang/dang-nhap/")
    Call<KhachHang> loginWithMap(@Body Map<String, String> body);
    @POST("api/user-auth/")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("api/user-auth/")
    Call<List<CustomerResponse>> getUsers();
    Call<List<UserModel>> getUsers(@Query("mode") String mode);

    // BỔ SUNG: Hàm đăng ký tài khoản (Dành cho RegisterActivity)
    @GET("api/user-auth/{id}/")
    Call<CustomerResponse> getUserAuthDetail(@Path("id") String id);

    @POST("api/user-auth/")
    Call<UserModel> createUser(@Query("mode") String mode, @Body UserModel user);

    @PUT("api/user-auth/{id}/")
    Call<UserModel> updateUser(@Path("id") String id, @Body UserModel user);

    @POST("api/user-auth/")
    Call<Void> registerAuth(@Body Map<String, String> data);

    @GET("api/taixe/")
    Call<List<Driver>> getDrivers();

    @GET("api/user-auth/{id}/")
    Call<CustomerResponse> getUserAuthDetail(@Path("id") String id);

    // --- 2. QUẢN LÝ NHÀ XE ---
    @POST("api/nhaxe/")
    Call<Void> createNhaXeProfile(@Body Map<String, String> data);

    @GET("api/nhaxe/{id}/")
    Call<NhaXe> getNhaXeDetail(@Path("id") String id);

    @PUT("api/nhaxe/{id}/")
    Call<Void> updateNhaXeProfile(@Path("id") String id, @Body Map<String, String> data);

    @PATCH("api/nhaxe/{id}/")
    Call<Void> patchNhaXeProfile(@Path("id") String id, @Body Map<String, Object> data);

    // --- 3. QUẢN LÝ PHƯƠNG TIỆN ---
    @GET("api/xe/")
    Call<List<VehicleManaged>> getVehicles();

    @PUT("api/xe/{id}/")
    Call<VehicleManaged> updateVehicle(@Path("id") String id, @Body VehicleManaged vehicle);

    @DELETE("api/xe/{id}/")
    Call<Void> deleteVehicle(@Path("id") String id);

    // --- 4. QUẢN LÝ TUYẾN XE ---
    @GET("api/tuyenxe/")
    Call<List<Route>> getRoutes();

    @PUT("api/tuyenxe/{id}/")
    Call<Void> updateRoute(@Path("id") String id, @Body Map<String, String> data);
    // --- 5. TÀI XẾ ---
    @GET("api/taixe/")
    Call<List<TaixeModel>> getTaiXeList(@Query("mode") String mode);

    @POST("api/taixe/")
    Call<TaixeModel> createTaiXe(@Query("mode") String mode, @Body TaixeModel taixe);

    @DELETE("api/tuyenxe/{id}/")
    Call<Void> deleteRoute(@Path("id") String id);
    @PUT("api/taixe/{id}/")
    Call<TaixeModel> updateTaiXe(@Path("id") String id, @Body TaixeModel taixe);

    @DELETE("api/taixe/{id}/")
    Call<Void> deleteTaiXe(@Path("id") String id);

    @GET("api/chitiettaixe/")
    Call<List<ChiTietTaiXeModel>> getChiTietTaiXeList(@Query("mode") String mode);

    @PUT("api/chitiettaixe/{id}/")
    Call<ChiTietTaiXeModel> updateChiTietTaiXe(@Path("id") String id, @Body ChiTietTaiXeModel chiTiet);

    @POST("api/chitiettaixe/")
    Call<ChiTietTaiXeModel> createChiTietTaiXe(@Query("mode") String mode, @Body ChiTietTaiXeModel chiTiet);

    @Multipart
    @POST("api/khachhang/")
    Call<Void> createKhachHangProfile(
            @Part("KhachHangID") RequestBody id,
            @Part("hoTen") RequestBody name,
            @Part("Email") RequestBody email,
            @Part("Ngaysinh") RequestBody dob,
            @Part MultipartBody.Part imageFile
    );
    @GET("api/taixe/")
    Call<List<Map<String, Object>>> getDriversRaw();

    @GET("api/chitiettaixe/")
    Call<List<Map<String, Object>>> getChiTietTaiXe();

    // --- 6. QUẢN LÝ KHÁCH HÀNG ---
    @GET("api/khachhang/")
    Call<List<Map<String, Object>>> getKhachHangList();

    @GET("api/khachhang/{id}/")
    Call<KhachHang> getProfile(@Path("id") String id);

    @GET("api/khachhang/{id}/")
    Call<Map<String, Object>> getKhachHangDetail(@Path("id") String id);

    @POST("api/khachhang/")
    Call<Void> createKhachHangProfile(@Body Map<String, String> data);

    @POST("api/khachhang/dang-ky/")
    Call<KhachHang> register(@Body Map<String, String> data);
    @DELETE("api/khachhang/{id}/")
    Call<Void> deleteKhachHang(@Path("id") String id);

    @Multipart
    @PUT("api/khachhang/{id}/")
    Call<Void> updateKhachHang(
            @Path("id") String id,
            @Part("hoTen") RequestBody name,
            @Part("Ngaysinh") RequestBody dob,
            @Part MultipartBody.Part imageFile
    );

    @PUT("api/khachhang/{id}/sua-thong-tin/")
    Call<KhachHang> updateProfile(@Path("id") String id, @Body KhachHang khachHang);

    @PATCH("api/khachhang/{id}/")
    Call<Void> patchKhachHang(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("api/khachhang/{id}/")
    Call<Void> deleteKhachHang(@Path("id") String id);

    // --- 7. QUẢN LÝ CHUYẾN XE ---
    @GET("api/chuyenxe/")
    Call<List<Trip>> getTrips();

    @POST("api/chuyenxe/")
    Call<Trip> createTrip(@Body Trip trip);

    @POST("api/chuyenxe/")
    Call<Void> createTripRaw(@Body Map<String, Object> data);

    @PUT("api/chuyenxe/{id}/")
    Call<Void> updateTripRaw(@Path("id") String id, @Body Map<String, Object> data);

    @PATCH("api/chuyenxe/{id}/")
    Call<Void> patchTrip(@Path("id") String id, @Body Map<String, Object> data);

    @GET("api/chuyenxe/")
    Call<List<TripSearchResult>> getChuyenXe();

    // --- 8. ĐẶT VÉ & GHẾ NGỒI ---
    @GET("api/ghengoi/")
    Call<List<Seat>> getSeatsByTrip(@Query("ChuyenXe") String chuyenXeId);

    @POST("api/dat-ve/")
    Call<Void> bookTicket(@Body BookingRequest bookingRequest);

    @POST("api/ghengoi/")
    Call<Void> createGheNgoi(@Body Map<String, Object> data);

    // --- 9. VÉ XE ---
    @GET("api/ve/")
    Call<List<Map<String, Object>>> getTicketsByTrip(@Query("ChuyenXe") String tripId);

    @PATCH("api/ve/{id}/")
    Call<Void> patchTicket(@Path("id") String id, @Body Map<String, Object> data);

    // --- 10. LOẠI XE ---
    @GET("api/loaixe/")
    Call<List<Loaixe>> getLoaixe();

    @PUT("api/loaixe/{id}/")
    Call<Loaixe> updateLoaixe(@Path("id") String id, @Body Loaixe loaixe);
}
