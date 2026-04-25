package com.example.nhom7vexeapp.models;

import com.example.nhom7vexeapp.User;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserResponse {
    @SerializedName("results")
    private List<User> results;

    @SerializedName("count")
    private int count;

    public List<User> getResults() {
        return results;
    }

    public void setResults(List<User> results) {
        this.results = results;
    }

    public int getCount() {
        return count;
    }
}
