package com.example.nhom7vexeapp.models;

import java.util.Objects;

public class SearchHistory {
    private String origin;
    private String dest;
    private String date;
    private String time;

    public SearchHistory(String origin, String dest, String date, String time) {
        this.origin = origin;
        this.dest = dest;
        this.date = date;
        this.time = time;
    }

    public String getOrigin() { return origin; }
    public String getDest() { return dest; }
    public String getDate() { return date; }
    public String getTime() { return time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchHistory that = (SearchHistory) o;
        return Objects.equals(origin, that.origin) &&
                Objects.equals(dest, that.dest) &&
                Objects.equals(date, that.date) &&
                Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, dest, date, time);
    }
}
