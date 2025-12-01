package com.example.duan1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.duan1.model.Response;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class TrangChu extends AppCompatActivity {
    TextView tvTongSanPham, tvDoanhThu;
    CardView cardTongSanPham, cardDoanhThu, cardDonHangMoi, cardTinhGiaMoi;
    TextInputEditText edtSearch;
    private ApiServices apiServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trang_chu);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ các view
        tvTongSanPham = findViewById(R.id.tvTongSanPham);
        tvDoanhThu = findViewById(R.id.tvDoanhThu);
        cardTongSanPham = findViewById(R.id.cardTongSanPham);
        cardDoanhThu = findViewById(R.id.cardDoanhThu);
        cardDonHangMoi = findViewById(R.id.cardDonHangMoi);
        cardTinhGiaMoi = findViewById(R.id.cardTinhGiaMoi);
        edtSearch = findViewById(R.id.edtSearch);

        // Load dữ liệu từ API
        loadDashboardData();

        // Xử lý tìm kiếm
        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: Xử lý tìm kiếm
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Xử lý click các card
        cardTongSanPham.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrangChu.this, XemSanPham.class);
                startActivity(intent);
            }
        });

        cardDoanhThu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TrangChu.this, "Xem chi tiết doanh thu", Toast.LENGTH_SHORT).show();
            }
        });

        cardDonHangMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TrangChu.this, QuanLyDonHang.class);
                startActivity(intent);
            }
        });

        cardTinhGiaMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TrangChu.this, "Tính giá mới", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDashboardData() {
        // Load tổng sản phẩm
        apiServices.getTotalProducts().enqueue(new Callback<Response<Map<String, Integer>>>() {
            @Override
            public void onResponse(Call<Response<Map<String, Integer>>> call, retrofit2.Response<Response<Map<String, Integer>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Map<String, Integer>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        Integer total = res.getData().get("total");
                        if (total != null) {
                            tvTongSanPham.setText(String.valueOf(total));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<Map<String, Integer>>> call, Throwable t) {
                Log.e("API Error", t.toString());
            }
        });

        // Load doanh thu hôm nay
        apiServices.getTodayRevenue().enqueue(new Callback<Response<Map<String, Double>>>() {
            @Override
            public void onResponse(Call<Response<Map<String, Double>>> call, retrofit2.Response<Response<Map<String, Double>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Map<String, Double>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        Double revenue = res.getData().get("revenue");
                        if (revenue != null) {
                            // Format: 50Md (50 triệu)
                            long millions = (long) (revenue / 1000000);
                            tvDoanhThu.setText(millions + "Md");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<Map<String, Double>>> call, Throwable t) {
                Log.e("API Error", t.toString());
            }
        });
    }
}
