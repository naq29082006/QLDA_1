package com.example.duan1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.Order;
import com.example.duan1.model.Response;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class QuanLyDonHang extends AppCompatActivity {
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FrameLayout imgIcon;
    private ApiServices apiServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_don_hang);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ các view
        rvOrders = findViewById(R.id.rvOrders);
        imgIcon = findViewById(R.id.imgIcon);

        // Khởi tạo danh sách đơn hàng
        orderList = new ArrayList<>();

        // Setup RecyclerView
        orderAdapter = new OrderAdapter(this, orderList);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);

        // Load đơn hàng từ API
        loadOrders();

        // Xử lý click icon
        imgIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Xử lý click chi tiết đơn hàng
        orderAdapter.setOnDetailClickListener(new OrderAdapter.OnDetailClickListener() {
            @Override
            public void onDetailClick(Order order) {
                Toast.makeText(QuanLyDonHang.this, "Chi tiết đơn hàng: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
                // TODO: Chuyển đến màn hình chi tiết đơn hàng
            }
        });
    }

    private void loadOrders() {
        apiServices.getOrders().enqueue(new Callback<Response<List<Order>>>() {
            @Override
            public void onResponse(Call<Response<List<Order>>> call, retrofit2.Response<Response<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Order>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        orderList.clear();
                        orderList.addAll(res.getData());
                        orderAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(QuanLyDonHang.this, "Không có đơn hàng nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QuanLyDonHang.this, "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", response.toString());
                }
            }

            @Override
            public void onFailure(Call<Response<List<Order>>> call, Throwable t) {
                Toast.makeText(QuanLyDonHang.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", t.toString());
            }
        });
    }
}
