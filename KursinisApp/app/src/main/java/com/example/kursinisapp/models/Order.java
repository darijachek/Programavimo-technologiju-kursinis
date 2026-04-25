package com.example.kursinisapp.models;

import com.google.gson.annotations.SerializedName;

public class Order {
    public Long id;

    @SerializedName(value = "userId", alternate = {"user_id", "clientId", "client_id"})
    public Long userId;

    @SerializedName(value = "restaurantId", alternate = {"restaurant_id"})
    public Long restaurantId;

    @SerializedName(value = "restaurantName", alternate = {"restaurant_name", "restaurant"})
    public String restaurantName;

    @SerializedName(value = "driverId", alternate = {"driver_id"})
    public Long driverId;

    @SerializedName(value = "totalPrice", alternate = {"total_price", "price", "total"})
    public double totalPrice;

    public String status;

    @SerializedName(value = "createdAt", alternate = {"created_at", "date"})
    public String createdAt;
}
