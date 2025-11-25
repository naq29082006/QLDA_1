package com.example.duan1;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.Product;
import com.example.duan1.model.Response;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class XemSanPham extends AppCompatActivity {
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private TextInputEditText edtSearch;
    private TextView tvQuantity;
    private Button btnMinus, btnPlus;
    private int quantity = 0;
    private LinearLayout navTrangChu, navBoSuuTap, navTaiKhoan, navDonHang, navMenu;
    private ApiServices apiServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xem_san_pham);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();
        
        // Ánh xạ các view
        rvProducts = findViewById(R.id.rvProducts);
        edtSearch = findViewById(R.id.edtSearch);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        
        // Bottom navigation
        navTrangChu = findViewById(R.id.navTrangChu);
        navBoSuuTap = findViewById(R.id.navBoSuuTap);
        navTaiKhoan = findViewById(R.id.navTaiKhoan);
        navDonHang = findViewById(R.id.navDonHang);
        navMenu = findViewById(R.id.navMenu);

        // Khởi tạo danh sách sản phẩm
        productList = new ArrayList<>();

        // Setup RecyclerView
        productAdapter = new ProductAdapter(this, productList);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);

        // Load sản phẩm từ API
        loadProducts();

        // Xử lý tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    loadProducts();
                } else {
                    searchProducts(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý số lượng
        btnMinus.setOnClickListener(v -> {
            if (quantity > 0) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        // Xử lý thêm vào giỏ hàng
        productAdapter.setOnAddToCartClickListener(product -> {
            Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            // TODO: Thêm logic thêm vào giỏ hàng
        });

        // Xử lý bottom navigation
        navTrangChu.setOnClickListener(v -> {
            finish();
        });

        navBoSuuTap.setOnClickListener(v -> {
            Toast.makeText(this, "Bộ sưu tập", Toast.LENGTH_SHORT).show();
        });

        navTaiKhoan.setOnClickListener(v -> {
            Toast.makeText(this, "Tài khoản", Toast.LENGTH_SHORT).show();
        });

        navDonHang.setOnClickListener(v -> {
            Toast.makeText(this, "Đơn hàng", Toast.LENGTH_SHORT).show();
        });

        navMenu.setOnClickListener(v -> {
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProducts() {
        apiServices.getProducts().enqueue(new Callback<Response<List<Product>>>() {
            @Override
            public void onResponse(Call<Response<List<Product>>> call, retrofit2.Response<Response<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Product>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        productList.clear();
                        productList.addAll(res.getData());
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(XemSanPham.this, "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(XemSanPham.this, "Lỗi khi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", response.toString());
                }
            }

            @Override
            public void onFailure(Call<Response<List<Product>>> call, Throwable t) {
                Toast.makeText(XemSanPham.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", t.toString());
            }
        });
    }

    private void searchProducts(String keyword) {
        apiServices.searchProducts(keyword).enqueue(new Callback<Response<List<Product>>>() {
            @Override
            public void onResponse(Call<Response<List<Product>>> call, retrofit2.Response<Response<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Product>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        productList.clear();
                        productList.addAll(res.getData());
                        productAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<List<Product>>> call, Throwable t) {
                Log.e("API Error", t.toString());
            }
        });
    }
}
