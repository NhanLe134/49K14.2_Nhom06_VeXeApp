package com.example.nhom7vexeapp.models;

public class CarType {
    private String name;
    private int seats;
    private String price;
    private String lastUpdate;
    private int color;

    public CarType(String name, int seats, String price, String lastUpdate, int color) {
        this.name = name;
        this.seats = seats;
        this.price = price;
        this.lastUpdate = lastUpdate;
        this.color = color;
    }

    // Các hàm để lấy dữ liệu
    public String getName() { return name; }
    public int getSeats() { return seats; }
    public String getPrice() { return price; }
    public String getLastUpdate() { return lastUpdate; }
    public int getColor() { return color; }
}
