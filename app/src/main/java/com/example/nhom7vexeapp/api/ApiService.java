package com.example.nhom7vexeapp.api;

import com.example.nhom7vexeapp.models.BookingRequest;
import com.example.nhom7vexeapp.models.KhachHang;
import com.example.nhom7vexeapp.models.Loaixe;
import com.example.nhom7vexeapp.models.Seat;
import com.example.nhom7vexeapp.models.Ticket;
import com.example.nhom7vexeapp.models.Trip;
import com.example.nhom7vexeapp.models.TripSearchResult;
import com.example.nhom7vexeapp.models.TaixeModel;
import com.example.nhom7vexeapp.models.ChiTietTaiXeModel;
import com.example.nhom7vexeapp.models.UserModel;
import com.example.nhom7vexeapp.models.NhaXe;
import com.example.nhom7vexeapp.models.Route;
import com.example.nhom7vexeapp.models.VehicleManaged;
import com.example.nhom7vexeapp.TicketModel;
import com.example.nhom7vexeapp.api.CustomerResponse;
import com.example.nhom7vexeapp.api.LoginResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // --- 1. XÁC THỰC & NGƯỜI DÙNG (AUTH & USER) ---
    @POST("api/user-auth/")
    Call<LoginResponse> login(@Body com.example.nhom7vexeapp.models.LoginRequest loginRequest);

    @GET("api/user-auth/")
    Call<List<CustomerResponse>> getUsers();

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

    @POST("api/user-auth/")
    Call<KhachHang> loginWithMap(@Body Map<String, String> body);


    // --- 2. QUẢN LÝ TÀI XẾ (DRIVER) ---
    @GET("api/taixe/")
    Call<List<TaixeModel>> getTaixeList(@Query("Method") String method);

    // --- 3. QUẢN LÝ PHƯƠNG TIỆN ---
    @GET("api/xe/")
    Call<List<VehicleManaged>> getVehicles();

    @POST("api/xe/")
    Call<Void> createVehicle(@Body Map<String, Object> data);

    @PUT("api/xe/{id}/")
    Call<VehicleManaged> updateVehicle(@Path("id") String id, @Body VehicleManaged vehicle);

    @DELETE("api/xe/{id}/")
    Call<Void> deleteVehicle(@Path("id") String id);

    // --- 4. QUẢN LÝ TUYẾN XE ---
    @GET("api/tuyenxe/")
    Call<List<Route>> getRoutes();

    @POST("api/tuyenxe/")
    Call<Void> createRoute(@Body Map<String, String> data);

    @PUT("api/tuyenxe/{id}/")
    Call<Void> updateRoute(@Path("id") String id, @Body Map<String, String> data);

    @DELETE("api/tuyenxe/{id}/")
    Call<Void> deleteRoute(@Path("id") String id);

    // --- 5. TÀI XẾ ---
    @GET("api/taixe/")
    Call<List<Map<String, Object>>> getDriversRaw();

    @POST("api/taixe/")
    Call<TaixeModel> createTaixe(@Query("Method") String method, @Body TaixeModel taixe);

    @PUT("api/taixe/{id}/")
    Call<TaixeModel> updateTaixe(@Path("id") String id, @Body TaixeModel taixe);

    @DELETE("api/taixe/{id}/")
    Call<Void> deleteTaixe(@Path("id") String id);

    // Chi tiết tài xế
    @GET("api/chitiettaixe/")
    Call<List<Map<String, Object>>> getChiTietTaiXe();

    @GET("api/chitiettaixe/")
    Call<List<ChiTietTaiXeModel>> getChiTietTaiXeList(@Query("Method") String method);

    @POST("api/chitiettaixe/")
    Call<ChiTietTaiXeModel> createChiTietTaiXe(@Query("Method") String method, @Body ChiTietTaiXeModel chiTiet);

    @PUT("api/chitiettaixe/{id}/")
    Call<ChiTietTaiXeModel> updateChiTietTaiXe(@Path("id") String id, @Body ChiTietTaiXeModel chiTiet);


    // --- 3. QUẢN LÝ CHUYẾN XE (TRIP) ---
    @GET("api/chuyenxe/")
    Call<List<TripSearchResult>> getChuyenXe();

    @GET("api/chuyenxe/")
    Call<List<Trip>> getTrips();

    @POST("api/chuyenxe/")
    Call<Trip> createTrip(@Body Trip trip);

    @POST("api/chuyenxe/")
    Call<List<Trip>> createTripList(@Body Trip trip);

    @POST("api/chuyenxe/")
    Call<Trip> createTripSingle(@Body Trip trip);

    @POST("api/chuyenxe/")
    Call<Void> createTripRaw(@Body Map<String, Object> data);

    @PUT("api/chuyenxe/{id}/")
    Call<Void> updateTrip(@Path("id") String id, @Body Trip trip);

    @PUT("api/chuyenxe/{id}/")
    Call<Void> updateTripRaw(@Path("id") String id, @Body Map<String, Object> data);

    @PATCH("api/chuyenxe/{id}/")
    Call<Void> patchTrip(@Path("id") String id, @Body Map<String, Object> data);


    // --- 4. ĐẶT VÉ & LỊCH SỬ (BOOKING & TICKETS) ---
    @POST("api/dat-ve/")
    Call<Void> bookTicket(@Body BookingRequest bookingRequest);

    @GET("api/ve/")
    Call<List<Ticket>> getTickets(
        @Query("KhachHang") String khachHangId,
        @Query("TrangThai") String trangThai
    );

    @GET("api/ve/")
    Call<List<Map<String, Object>>> getTicketsByTrip(@Query("ChuyenXe") String tripId);

    @GET("api/ve/")
    Call<List<Map<String, Object>>> getAllTickets();

    @PATCH("api/ve/{id}/")
    Call<Void> patchTicket(@Path("id") String id, @Body Map<String, Object> data);

    @DELETE("api/ve/{id}/")
    Call<Void> deleteTicket(@Path("id") String id);

    @GET("api/khachhang/{id}/lich-su-ve/")
    Call<List<TicketModel>> getTicketHistory(@Path("id") String id);


    // --- 5. GHẾ NGỒI (SEATS) ---
    @GET("api/ghengoi/")
    Call<List<Seat>> getSeatsByTrip(@Query("ChuyenXe") String chuyenXeId);

    @POST("api/ghengoi/")
    Call<Void> createGheNgoi(@Body Map<String, Object> data);


    // --- 6. TUYẾN XE (ROUTE) ---
    @GET("api/tuyenxe/")
    Call<List<Map<String, Object>>> getRoutesRaw();

    @GET("api/tuyenxe/")
    Call<List<Route>> getRoutesModel();


    // --- 7. QUẢN LÝ KHÁCH HÀNG (CUSTOMER) ---
    @GET("api/khachhang/")
    Call<List<Map<String, Object>>> getKhachHangList();

    @POST("api/khachhang/")
    Call<KhachHang> register(@Body Map<String, String> data);

    @POST("api/khachhang/")
    Call<Void> createKhachHangProfile(@Body Map<String, String> data);

    @Multipart
    @POST("api/khachhang/")
    Call<Void> createKhachHangProfileMultipart(
            @Part("KhachHangID") RequestBody id,
            @Part("hoTen") RequestBody name,
            @Part("Email") RequestBody email,
            @Part("Ngaysinh") RequestBody dob,
            @Part MultipartBody.Part imageFile
    );

    @GET("api/khachhang/{id}/")
    Call<Map<String, Object>> getKhachHangDetail(@Path("id") String id);

    @GET("api/khachhang/{id}/")
    Call<KhachHang> getProfile(@Path("id") String id);

    @PUT("api/khachhang/{id}/")
    Call<Void> updateKhachHang(@Path("id") String id, @Body Map<String, String> data);

    @PUT("api/khachhang/{id}/")
    Call<Void> updateKhachHangProfile(@Path("id") String id, @Body Map<String, String> data);

    @PUT("api/khachhang/{id}/")
    Call<KhachHang> updateProfile(@Path("id") String id, @Body KhachHang khachHang);

    @DELETE("api/khachhang/{id}/")
    Call<Void> deleteKhachHang(@Path("id") String id);


    // --- 8. QUẢN LÝ NHÀ XE (OPERATOR) ---
    @POST("api/nhaxe/")
    Call<Void> createNhaXeProfile(@Body Map<String, String> data);

    @GET("api/nhaxe/{id}/")
    Call<NhaXe> getNhaXeDetail(@Path("id") String id);

    @GET("api/nhaxe/{id}/")
    Call<Map<String, Object>> getNhaXeDetailRaw(@Path("id") String id);

    @PUT("api/nhaxe/{id}/")
    Call<Void> updateNhaXeProfile(@Path("id") String id, @Body Map<String, String> data);

    @PATCH("api/nhaxe/{id}/")
    Call<Void> patchNhaXeProfile(@Path("id") String id, @Body Map<String, Object> data);

    // --- 9. XE & LOẠI XE (VEHICLES) ---
    @GET("api/xe/")
    Call<List<Map<String, Object>>> getVehiclesRaw();

    // --- 10. LOẠI XE ---
    @GET("api/loaixe/")
    Call<List<Loaixe>> getLoaixe();

    @PUT("api/loaixe/{id}/")
    Call<Loaixe> updateLoaixe(@Path("id") String id, @Body Loaixe loaixe);

    // --- 10. ĐÁNH GIÁ (FEEDBACK) ---
    @GET("api/danhgia/")
    Call<List<Map<String, Object>>> getFeedbacks();

    @POST("api/danhgia/")
    Call<Void> sendFeedback(@Body Map<String, Object> data);
}
