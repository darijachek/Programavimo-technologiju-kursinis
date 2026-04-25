package com.example.kursinisapp.remote;

import com.example.kursinisapp.models.MenuItem;
import com.example.kursinisapp.models.Message;
import com.example.kursinisapp.models.Order;
import com.example.kursinisapp.models.Restaurant;
import com.example.kursinisapp.models.User;

import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/login")
    Call<ResponseBody> login(@Body Map<String, String> credentials);

    @POST("/register")
    Call<ResponseBody> register(@Body Map<String, Object> userData);

    @GET("/chat/{orderId}")
    Call<List<Message>> getChatMessages(@Path("orderId") Long orderId);

    @POST("/messages/send")
    Call<ResponseBody> sendMessage(@Body Map<String, Object> body);

    @GET("/restaurants")
    Call<List<Restaurant>> getRestaurants();

    @GET("/restaurant/{id}/menu")
    Call<List<MenuItem>> getMenu(@Path("id") Long id);

    @POST("/createOrder")
    Call<ResponseBody> createOrder(@Body Map<String, Object> orderRequest);

    @GET("/orders/client/{clientId}")
    Call<List<Order>> getClientOrders(@Path("clientId") Long clientId);

    @GET("/orders/driver/{driverId}")
    Call<List<Order>> getDriverOrders(@Path("driverId") Long driverId);

    @GET("/orders/available")
    Call<List<Order>> getAvailableOrders();

    @POST("/orders/take")
    Call<ResponseBody> takeOrder(@Body Map<String, Object> body);

    @POST("/orders/{orderId}/complete")
    Call<ResponseBody> completeOrder(@Path("orderId") Long orderId, @Body Map<String, Long> body);

    @GET("/orders/{id}")
    Call<Order> getOrder(@Path("id") Long id);

    @POST("/orders/pickup")
    Call<ResponseBody> pickupOrder(@Body Map<String, Object> body);

    @POST("/review/add")
    Call<ResponseBody> postReview(@Body Map<String, Object> reviewData);

    @PUT("/client/{id}/update-address")
    Call<ResponseBody> updateAddress(@Path("id") Long id, @Body Map<String, String> body);

    @PUT("/driver/{id}/update-vehicle")
    Call<ResponseBody> updateVehicle(@Path("id") Long id, @Body Map<String, String> body);

    @GET("/users/{id}")
    Call<User> getUser(@Path("id") Long id);

}