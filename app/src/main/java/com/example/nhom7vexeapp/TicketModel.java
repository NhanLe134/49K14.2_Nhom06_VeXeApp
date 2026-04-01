package com.example.nhom7vexeapp;

public class TicketModel {
    private String time;
    private String date;
    private String route;
    private String car;
    private String seatCount;
    private String seats;
    private String status; // Booked, Completed, Cancelled

    public TicketModel(String time, String date, String route, String car, String seatCount, String seats, String status) {
        this.time = time;
        this.date = date;
        this.route = route;
        this.car = car;
        this.seatCount = seatCount;
        this.seats = seats;
        this.status = status;
    }

    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getRoute() { return route; }
    public String getCar() { return car; }
    public String getSeatCount() { return seatCount; }
    public String getSeats() { return seats; }
    public String getStatus() { return status; }
}
