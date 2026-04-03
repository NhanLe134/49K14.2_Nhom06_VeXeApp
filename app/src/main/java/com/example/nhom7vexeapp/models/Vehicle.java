package com.example.nhom7vexeapp.models;

import java.io.Serializable;

public class Vehicle implements Serializable {
    private String plateNumber;
    private String type;
    private String status;
    private int seatCount;

    public Vehicle(String plateNumber, String type, String status, int seatCount) {
        this.plateNumber = plateNumber;
        this.type = type;
        this.status = status;
        this.seatCount = seatCount;
    }

    public String getPlateNumber() { return plateNumber; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public int getSeatCount() { return seatCount; }
}
