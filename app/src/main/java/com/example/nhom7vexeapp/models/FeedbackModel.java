package com.example.nhom7vexeapp.models;

import java.io.Serializable;

public class FeedbackModel implements Serializable {
    public String busName;
    public float rating;
    public String comment;
    public String date;
    public String route;

    public FeedbackModel(String busName, float rating, String comment, String date, String route) {
        this.busName = busName;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
        this.route = route;
    }
}
