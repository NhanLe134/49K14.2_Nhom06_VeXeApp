package com.example.nhom7vexeapp.utils;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class OSMRouteLibrary {

    public static class RouteInfo {
        public String distance;
        public String time;

        public RouteInfo(String distance, String time) {
            this.distance = distance;
            this.time = time;
        }
    }

    private static final Map<String, RouteInfo> routeData = new HashMap<>();

    static {
        // Tọa độ thực tế từ OSM (để tham khảo trong code nếu cần mở rộng)
        // Huế: 16.4637, 107.5909
        // Đà Nẵng: 16.0544, 108.2022
        // Hội An: 15.8801, 108.3382

        // Cài đặt dữ liệu lộ trình thực tế (Road Distance) từ OSM
        addRoute("Huế", "Đà Nẵng", "93 km", "2 giờ 15 phút");
        addRoute("Đà Nẵng", "Huế", "93 km", "2 giờ 15 phút");
        
        addRoute("Đà Nẵng", "Hội An", "30 km", "1 giờ");
        addRoute("Hội An", "Đà Nẵng", "30 km", "1 giờ");
        
        addRoute("Huế", "Hội An", "122 km", "2 giờ 45 phút");
        addRoute("Hội An", "Huế", "122 km", "2 giờ 45 phút");
    }

    private static void addRoute(String start, String end, String dist, String time) {
        String key = normalize(start) + "_" + normalize(end);
        routeData.put(key, new RouteInfo(dist, time));
    }

    public static RouteInfo getRoute(String start, String end) {
        if (start == null || end == null) return null;
        String key = normalize(start) + "_" + normalize(end);
        return routeData.get(key);
    }

    // Hàm chuẩn hóa tiếng Việt: bỏ dấu, viết thường để tìm kiếm thông minh
    public static String normalize(String str) {
        if (str == null) return "";
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("")
                .toLowerCase()
                .replace("đ", "d")
                .trim();
    }
}
