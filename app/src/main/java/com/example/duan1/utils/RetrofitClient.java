package com.example.duan1.utils;

import com.example.duan1.model.CartItem;
import com.example.duan1.model.CartItemTypeAdapter;
import com.example.duan1.model.OrderDetail;
import com.example.duan1.model.OrderDetailTypeAdapter;
import com.example.duan1.model.Review;
import com.example.duan1.model.ReviewTypeAdapter;
import com.example.duan1.services.ApiServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance = null;
    private ApiServices apiServices;

    private RetrofitClient() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CartItem.class, new CartItemTypeAdapter())
                .registerTypeAdapter(OrderDetail.class, new OrderDetailTypeAdapter())
                .registerTypeAdapter(Review.class, new ReviewTypeAdapter())
                .create();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiServices.Url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        apiServices = retrofit.create(ApiServices.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiServices getApiServices() {
        return apiServices;
    }
}

