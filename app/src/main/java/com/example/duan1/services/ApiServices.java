package com.example.duan1.services;

import com.example.duan1.model.Order;
import com.example.duan1.model.Product;
import com.example.duan1.model.Response;
import com.example.duan1.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiServices {
    String Url = "http://192.168.0.114:3000/";
    
    // ==================== AUTH APIs ====================
    @POST("api/register")
    Call<Response<User>> register(@Body Map<String, String> body);
    
    @POST("api/login")
    Call<Response<User>> login(@Body Map<String, String> body);

    // ==================== PRODUCT APIs ====================
    @GET("api/products")
    Call<Response<List<Product>>> getProducts();
    
    @GET("api/products/{id}")
    Call<Response<Product>> getProductById(@Path("id") String id);
    
    @GET("api/products/search/{keyword}")
    Call<Response<List<Product>>> searchProducts(@Path("keyword") String keyword);

    // ==================== ORDER APIs ====================
    @GET("api/orders")
    Call<Response<List<Order>>> getOrders();
    
    @GET("api/orders/user/{userId}")
    Call<Response<List<Order>>> getOrdersByUserId(@Path("userId") String userId);
    
    @GET("api/orders/undelivered")
    Call<Response<List<Order>>> getUndeliveredOrders();
    
    @GET("api/orders/{id}")
    Call<Response<Order>> getOrderById(@Path("id") String id);

    // ==================== STATISTICS APIs ====================
    @GET("api/statistics/products/total")
    Call<Response<Map<String, Integer>>> getTotalProducts();
    
    @GET("api/statistics/products/top5")
    Call<Response<List<Product>>> getTop5Products();
    
    @GET("api/statistics/orders/rate")
    Call<Response<Map<String, Integer>>> getOrderRate();
    
    @GET("api/statistics/revenue/today")
    Call<Response<Map<String, Double>>> getTodayRevenue();
}
