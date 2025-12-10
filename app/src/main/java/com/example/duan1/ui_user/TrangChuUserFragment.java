package com.example.duan1.ui_user;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.duan1.AdapterUser.CategoryAdapter;
import com.example.duan1.AdapterUser.ProductAdapter;
import com.example.duan1.R;
import com.example.duan1.model.DanhMuc;
import com.example.duan1.model.Product;
import com.example.duan1.model.Response;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.PollingHelper;
import com.example.duan1.utils.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class TrangChuUserFragment extends Fragment {

    private RecyclerView rvProducts, rvCategories;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> productList;
    private List<DanhMuc> danhMucList;
    private TextInputEditText edtSearch;
    private ApiServices apiServices;
    private String selectedCategoryId = null;
    private PollingHelper pollingHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trang_chu_user, container, false);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ views
        rvProducts = view.findViewById(R.id.rvProducts);
        rvCategories = view.findViewById(R.id.rvCategories);
        edtSearch = view.findViewById(R.id.edtSearch);

        // Khởi tạo danh sách
        productList = new ArrayList<>();
        danhMucList = new ArrayList<>();

        // Setup RecyclerView Categories (ngang)
        categoryAdapter = new CategoryAdapter(getContext(), danhMucList);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
        
        // Xử lý click vào danh mục
        categoryAdapter.setOnCategoryClickListener(categoryId -> {
            selectedCategoryId = categoryId;
            if (categoryId == null) {
                // Click "All"
                loadProducts();
            } else {
                // Click danh mục cụ thể
                loadProductsByCategory(categoryId);
            }
        });

        // Setup RecyclerView Products với GridLayout 2 cột
        productAdapter = new ProductAdapter(getContext(), productList);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(productAdapter);

        // Xử lý click vào sản phẩm
        productAdapter.setOnProductClickListener(product -> {
            showChiTietSanPhamDialog(product);
        });

        // Load danh mục và sản phẩm từ API
        loadCategories();
        loadProducts();

        // Khởi tạo PollingHelper để tự động cập nhật mỗi 5 giây
        pollingHelper = new PollingHelper("TrangChuUser", 5000);
        pollingHelper.setRefreshCallback(() -> {
            // Chỉ refresh nếu không đang search
            if (edtSearch.getText().toString().trim().isEmpty()) {
                loadCategories();
                if (selectedCategoryId != null) {
                    loadProductsByCategory(selectedCategoryId);
                } else {
                    loadProducts();
                }
            }
        });
        pollingHelper.startPolling();

        // Xử lý tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    if (selectedCategoryId != null) {
                        loadProductsByCategory(selectedCategoryId);
                    } else {
                        loadProducts();
                    }
                } else {
                    searchProducts(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadCategories() {
        apiServices.getAllCategories().enqueue(new Callback<Response<List<DanhMuc>>>() {
            @Override
            public void onResponse(Call<Response<List<DanhMuc>>> call, retrofit2.Response<Response<List<DanhMuc>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<DanhMuc>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        danhMucList.clear();
                        danhMucList.addAll(res.getData());
                        categoryAdapter.updateList(danhMucList);
                        productAdapter.setDanhMucList(danhMucList);
                        Log.d("API", "Loaded " + danhMucList.size() + " categories");
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<List<DanhMuc>>> call, Throwable t) {
                Log.e("API Error", "Load categories failure: " + t.getMessage());
            }
        });
    }

    private void loadProducts() {
        selectedCategoryId = null;
        categoryAdapter.setSelectedPosition(0); // Chọn "All"
        apiServices.getAllProducts().enqueue(new Callback<Response<List<Product>>>() {
            @Override
            public void onResponse(Call<Response<List<Product>>> call, retrofit2.Response<Response<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Product>> res = response.body();
                    Log.d("API Response", "Success: " + res.isSuccess() + ", Data: " + (res.getData() != null ? res.getData().size() : "null"));
                    if (res.isSuccess() && res.getData() != null && !res.getData().isEmpty()) {
                        productList.clear();
                        productList.addAll(res.getData());
                        productAdapter.updateList(productList);
                        Log.d("API", "Loaded " + productList.size() + " products");
                    } else {
                        String message = res.getMessage() != null ? res.getMessage() : "Không có sản phẩm nào";
                        Log.d("API", "No products: " + message);
                    }
                } else {
                    String errorMsg = "Lỗi khi tải sản phẩm";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("API Error", "Response code: " + response.code() + ", Message: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Response<List<Product>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Failure: " + t.getMessage(), t);
            }
        });
    }

    private void loadProductsByCategory(String categoryId) {
        apiServices.getProductsByCategory(categoryId).enqueue(new Callback<Response<List<Product>>>() {
            @Override
            public void onResponse(Call<Response<List<Product>>> call, retrofit2.Response<Response<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Product>> res = response.body();
                    if (res.isSuccess() && res.getData() != null && !res.getData().isEmpty()) {
                        productList.clear();
                        productList.addAll(res.getData());
                        productAdapter.updateList(productList);
                        Log.d("API", "Loaded " + productList.size() + " products by category");
                    } else {
                        // Không có sản phẩm trong danh mục này, chỉ clear list
                        productList.clear();
                        productAdapter.updateList(productList);
                        Log.d("API", "No products in this category");
                    }
                } else {
                    // Lỗi nhưng không báo, chỉ clear list
                    productList.clear();
                    productAdapter.updateList(productList);
                    Log.e("API Error", "Load products by category failed: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<Response<List<Product>>> call, Throwable t) {
                // Lỗi nhưng không báo, chỉ clear list
                productList.clear();
                productAdapter.updateList(productList);
                Log.e("API Error", "Load products by category failure: " + t.getMessage());
            }
        });
    }

    private void searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadProducts();
            return;
        }
        
        apiServices.searchProducts(keyword.trim()).enqueue(new Callback<Response<List<Product>>>() {
            @Override
            public void onResponse(Call<Response<List<Product>>> call, retrofit2.Response<Response<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Product>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        productList.clear();
                        productList.addAll(res.getData());
                        productAdapter.updateList(productList);
                        Log.d("API", "Search found " + productList.size() + " products");
                    } else {
                        productList.clear();
                        productAdapter.updateList(productList);
                    }
                } else {
                    Log.e("API Error", "Search failed: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<Response<List<Product>>> call, Throwable t) {
                Log.e("API Error", "Search failure: " + t.getMessage());
            }
        });
    }

    private void showChiTietSanPhamDialog(Product product) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chi_tiet_san_pham_user);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Ánh xạ views
        ImageView imgChiTiet = dialog.findViewById(R.id.imgChiTietSanPham);
        TextView tvTen = dialog.findViewById(R.id.tvChiTietTen);
        TextView tvMoTa = dialog.findViewById(R.id.tvChiTietMoTa);
        TextView tvDanhMuc = dialog.findViewById(R.id.tvChiTietDanhMuc);
        TextView tvGia = dialog.findViewById(R.id.tvChiTietGia);
        TextView tvSoLuong = dialog.findViewById(R.id.tvSoLuong);
        Button btnTang = dialog.findViewById(R.id.btnTang);
        Button btnGiam = dialog.findViewById(R.id.btnGiam);
        Button btnThemVaoGio = dialog.findViewById(R.id.btnThemVaoGio);
        Button btnDong = dialog.findViewById(R.id.btnDong);

        // Hiển thị thông tin sản phẩm
        tvTen.setText(product.getName());
        tvMoTa.setText(product.getDescription() != null ? product.getDescription() : "Không có mô tả");
        tvGia.setText(product.getFormattedPrice());
        
        // Hiển thị danh mục
        String categoryName = "Chưa phân loại";
        if (product.getCategoryId() != null && danhMucList != null) {
            for (DanhMuc danhMuc : danhMucList) {
                if (danhMuc.getId() != null && danhMuc.getId().equals(product.getCategoryId())) {
                    categoryName = danhMuc.getName();
                    break;
                }
            }
        }
        tvDanhMuc.setText(categoryName);

        // Load ảnh
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imageUrl = product.getImage();
            if (!imageUrl.startsWith("http")) {
                imageUrl = ApiServices.Url.replace("/api/", "") + imageUrl;
            }
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(imgChiTiet);
        } else {
            imgChiTiet.setImageResource(R.drawable.ic_home);
        }

        // Xử lý số lượng
        int[] soLuong = {1};
        tvSoLuong.setText(String.valueOf(soLuong[0]));

        btnTang.setOnClickListener(v -> {
            soLuong[0]++;
            tvSoLuong.setText(String.valueOf(soLuong[0]));
        });

        btnGiam.setOnClickListener(v -> {
            if (soLuong[0] > 1) {
                soLuong[0]--;
                tvSoLuong.setText(String.valueOf(soLuong[0]));
            }
        });

        // Thêm vào giỏ hàng
        btnThemVaoGio.setOnClickListener(v -> {
            // Lấy user_id từ SharedPreferences (lưu từ Dangnhap.java)
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("UserData", getContext().MODE_PRIVATE);
            String userId = prefs.getString("id_taikhoan", null);
            
            // Debug
            java.util.Map<String, ?> allPrefs = prefs.getAll();
            Log.d("SharedPreferences", "All keys: " + allPrefs.keySet().toString());
            Log.d("SharedPreferences", "id_taikhoan value: " + userId);
            
            if (userId == null || userId.isEmpty()) {
                Log.e("Error", "User ID not found. Available keys: " + allPrefs.keySet());
                Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            // Gọi API thêm vào giỏ hàng
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put("user_id", userId);
            body.put("product_id", product.getId());
            body.put("quantity", String.valueOf(soLuong[0]));

            apiServices.addToCart(body).enqueue(new retrofit2.Callback<com.example.duan1.model.Response<com.example.duan1.model.CartItem>>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.duan1.model.Response<com.example.duan1.model.CartItem>> call, retrofit2.Response<com.example.duan1.model.Response<com.example.duan1.model.CartItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.duan1.model.Response<com.example.duan1.model.CartItem> res = response.body();
                        if (res.isSuccess()) {
                            Toast.makeText(getContext(), "Đã thêm " + soLuong[0] + "x " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.duan1.model.Response<com.example.duan1.model.CartItem>> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Đóng dialog
        btnDong.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pollingHelper != null) {
            pollingHelper.stopPolling();
        }
    }
}

