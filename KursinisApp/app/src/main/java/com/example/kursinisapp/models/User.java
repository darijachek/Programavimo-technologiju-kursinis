package com.example.kursinisapp.models;

import com.google.gson.annotations.SerializedName;

public class User {
    public Long id;
    @SerializedName(value = "username", alternate = {"userName", "name"})
    public String username;
    public String password;
    public String role;

    @SerializedName(value = "loyaltyPoints", alternate = {"loyalty_points", "points", "loyalty"})
    public int loyaltyPoints;
    public String address;
    public String vehicle;
}
