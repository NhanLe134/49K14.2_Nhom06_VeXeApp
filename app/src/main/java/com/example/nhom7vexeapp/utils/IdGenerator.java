package com.example.nhom7vexeapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class IdGenerator {
    private static final String PREF_NAME = "AppConfig";
    private static final String KEY_CUSTOMER_COUNT = "customer_count";

    public static String generateKhachHangID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Lấy số lượng khách hàng hiện tại (mặc định là 0 nếu chưa có ai)
        int currentCount = prefs.getInt(KEY_CUSTOMER_COUNT, 0);
        
        // Tăng số lượng lên 1 cho người mới
        int newCount = currentCount + 1;
        
        // Lưu lại số lượng mới vào cấu hình
        prefs.edit().putInt(KEY_CUSTOMER_COUNT, newCount).apply();
        
        // Định dạng mã theo kiểu KH + 5 chữ số (ví dụ: KH00001)
        return String.format("KH%05d", newCount);
    }
}
