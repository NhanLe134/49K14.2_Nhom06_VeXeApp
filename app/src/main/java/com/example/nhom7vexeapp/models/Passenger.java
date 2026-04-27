package com.example.nhom7vexeapp.models;

import java.io.Serializable;

public class Passenger implements Serializable {
    private String name;
    private String phone;
    private String pickupPoint;
    private String dropoffPoint;
    private String seatNumber;

    public Passenger() {
    }

    public Passenger(String name, String phone, String pickupPoint, String dropoffPoint, String seatNumber) {
        this.name = name;
        this.phone = phone;
        this.pickupPoint = pickupPoint;
        this.dropoffPoint = dropoffPoint;
        this.seatNumber = seatNumber;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getPickupPoint() { return pickupPoint; }
    public String getDropoffPoint() { return dropoffPoint; }
    public String getSeatNumber() { return seatNumber; }
}
