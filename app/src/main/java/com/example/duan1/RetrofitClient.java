package com.example.duan1;

import com.example.duan1.services.ApiServices;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    public static Retrofit getInstance() {
if (retrofit ==null) {
    retrofit = new Retrofit.Builder()
            .baseUrl(ApiServices.Url)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
return retrofit;
}
}
