package com.example.nhom7vexeapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TripResponse {
    @SerializedName("results")
    private List<TripSearchResult> results;

    @SerializedName("count")
    private int count;

    public List<TripSearchResult> getResults() {
        return results;
    }

    public void setResults(List<TripSearchResult> results) {
        this.results = results;
    }
}
