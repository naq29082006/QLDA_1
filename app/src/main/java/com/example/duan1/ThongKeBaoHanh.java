package com.example.duan1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.Order;
import com.example.duan1.model.Product;
import com.example.duan1.model.Response;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class ThongKeBaoHanh extends AppCompatActivity {
    private RecyclerView rvTopProducts, rvTopUndelivered;
    private ProductAdapterForThongKe topProductAdapter;
    private OrderAdapter undeliveredAdapter;
    private List<Product> topProductList;
    private List<Order> undeliveredOrderList;
    private FrameLayout imgIcon;
    private ImageView imgBack;
    private ApiServices apiServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke_bao_hanh);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ các view
        rvTopProducts = findViewById(R.id.rvTopProducts);
        rvTopUndelivered = findViewById(R.id.rvTopUndelivered);
        imgIcon = findViewById(R.id.imgIcon);
        imgBack = findViewById(R.id.imgBack);
        
        // Xử lý nút back
        imgBack.setOnClickListener(v -> finish());

        // Khởi tạo danh sách
        topProductList = new ArrayList<>();
        undeliveredOrderList = new ArrayList<>();

        // Setup RecyclerView cho Top 5 sản phẩm bán chạy
        topProductAdapter = new ProductAdapterForThongKe(this, topProductList);
        rvTopProducts.setLayoutManager(new LinearLayoutManager(this));
        rvTopProducts.setAdapter(topProductAdapter);

        // Setup RecyclerView cho Top 5 đơn chưa giao
        undeliveredAdapter = new OrderAdapter(this, undeliveredOrderList);
        rvTopUndelivered.setLayoutManager(new LinearLayoutManager(this));
        rvTopUndelivered.setAdapter(undeliveredAdapter);

        // Load dữ liệu từ API
        loadTopProducts();
        loadUndeliveredOrders();

        // Xử lý click icon
        imgIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadTopProducts() {
//        apiServices.getTop5Products().enqueue(new Callback<Response<List<Product>>>() {
//            @Override
//            public void onResponse(Call<Response<List<Product>>> call, retrofit2.Response<Response<List<Product>>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    Response<List<Product>> res = response.body();
//                    if (res.isSuccess() && res.getData() != null) {
//                        topProductAdapter.updateList(res.getData());
//                    }
//                } else {
//                    Log.e("API Error", response.toString());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Response<List<Product>>> call, Throwable t) {
//                Log.e("API Error", t.toString());
//            }
//        });
    }

    private void loadUndeliveredOrders() {
//        apiServices.getUndeliveredOrders().enqueue(new Callback<Response<List<Order>>>() {
//            @Override
//            public void onResponse(Call<Response<List<Order>>> call, retrofit2.Response<Response<List<Order>>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    Response<List<Order>> res = response.body();
//                    if (res.isSuccess() && res.getData() != null) {
//                        undeliveredOrderList.clear();
//                        undeliveredOrderList.addAll(res.getData());
//                        undeliveredAdapter.notifyDataSetChanged();
//                    }
//                } else {
//                    Log.e("API Error", response.toString());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Response<List<Order>>> call, Throwable t) {
//                Log.e("API Error", t.toString());
//            }
//        });
    }
}
