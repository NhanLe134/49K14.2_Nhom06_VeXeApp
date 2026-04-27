package com.example.nhom7vexeapp.models;

public class TripSearchResult extends Trip {
    
    public TripSearchResult() {
        super();
    }

    public TripSearchResult(String id, String tuyenXeID, String date, String startTime, String status) {
        super(id, tuyenXeID, date, startTime, status);
    }

    public String getNhaXeName() { 
        String name = getTenNhaXe();
        return (name != null && !name.isEmpty()) ? name : "Nhà xe"; 
    }
    
    public String getPrice() { 
        String p = getGiaVe();
        return (p != null && !p.equals("0") && !p.isEmpty()) ? p : "---"; 
    }
    
    public String getCarType() { 
        String type = getVehicleType();
        // Nếu getVehicleType trả về "Xe: ..." (mặc định của lớp cha khi loaiXe null), trả về "Xe 4 chỗ"
        return (type != null && !type.startsWith("Xe: ")) ? type : "Xe 4 chỗ"; 
    }
    
    public String getTuyenXeName() {
        return getRouteName();
    }
    
    @Override
    public int getSeats() {
        return super.getSeats();
    }
}
