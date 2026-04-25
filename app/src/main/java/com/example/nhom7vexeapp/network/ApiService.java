package com.example.nhom7vexeapp.network;

import com.example.nhom7vexeapp.TicketModel;
import com.example.nhom7vexeapp.models.KhachHang;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("management/khachhang/dang-ky/")
    Call<KhachHang> register(@Body Map<String, String> body);

    @POST("management/khachhang/dang-nhap/")
    Call<KhachHang> login(@Body Map<String, String> body);

    @GET("management/khachhang/{id}/")
    Call<KhachHang> getProfile(@Path("id") String id);

    @PUT("management/khachhang/{id}/sua-thong-tin/")
    Call<KhachHang> updateProfile(@Path("id") String id, @Body KhachHang khachHang);

    @GET("management/khachhang/{id}/lich-su-ve/")
    Call<List<TicketModel>> getTicketHistory(@Path("id") String id);
}
