package com.example.duan1;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.duan1.model.Order;
import com.example.duan1.model.OrderDetail;
import com.example.duan1.model.Response;
import com.example.duan1.model.Review;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.PollingHelper;
import com.example.duan1.utils.RetrofitClient;
import com.example.duan1.OrderDetailAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class QuanLyDanhGia extends AppCompatActivity {

    private RecyclerView rvDanhGia;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ReviewAdapter adapter;
    private List<Review> reviewList;
    private TextInputEditText edtSearchDanhGia;
    private ImageView imgBack;
    private TextView tvEmpty;
    private ApiServices apiServices;
    private PollingHelper pollingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_danh_gia);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ views
        rvDanhGia = findViewById(R.id.rvDanhGia);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        edtSearchDanhGia = findViewById(R.id.edtSearchDanhGia);
        imgBack = findViewById(R.id.imgBack);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        // Set action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Đánh giá");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Xử lý nút back
        imgBack.setOnClickListener(v -> finish());

        // Khởi tạo danh sách
        reviewList = new ArrayList<>();

        // Setup RecyclerView
        adapter = new ReviewAdapter(this, reviewList);
        rvDanhGia.setLayoutManager(new LinearLayoutManager(this));
        rvDanhGia.setAdapter(adapter);

        // Xử lý click vào review để xem chi tiết
        adapter.setOnReviewClickListener(review -> {
            showChiTietDanhGiaDialog(review);
        });

        // Search
        edtSearchDanhGia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup SwipeRefreshLayout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadReviews(false);
            });
        }
        
        // Khởi tạo PollingHelper để tự động cập nhật mỗi 5 giây
        pollingHelper = new PollingHelper("QuanLyDanhGia", 5000);
        pollingHelper.setRefreshCallback(() -> {
            loadReviews(true); // Silent refresh
        });
        pollingHelper.startPolling();

        // Load reviews từ API
        loadReviews(false);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollingHelper != null) {
            pollingHelper.stopPolling();
        }
    }

    private void loadReviews() {
        loadReviews(false);
    }
    
    private void loadReviews(boolean silent) {
        apiServices.getAllReviews().enqueue(new Callback<Response<List<Review>>>() {
            @Override
            public void onResponse(Call<Response<List<Review>>> call, retrofit2.Response<Response<List<Review>>> response) {
                // Dừng refresh indicator
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Review>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        List<Review> newList = res.getData();
                        reviewList.clear();
                        reviewList.addAll(newList);
                        adapter.updateList(reviewList);
                        updateEmptyState();
                        if (!silent) {
                            Log.d("QuanLyDanhGia", "Loaded " + reviewList.size() + " reviews");
                        }
                    } else {
                        reviewList.clear();
                        adapter.updateList(reviewList);
                        updateEmptyState();
                        if (!silent) {
                            Log.d("QuanLyDanhGia", "No reviews: " + res.getMessage());
                        }
                    }
                } else {
                    String errorMsg = "Lỗi khi tải đánh giá";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("QuanLyDanhGia", "Load reviews failed: " + errorMsg);
                    if (!silent) {
                        Toast.makeText(QuanLyDanhGia.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<List<Review>>> call, Throwable t) {
                // Dừng refresh indicator
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                Log.e("QuanLyDanhGia", "Load reviews failure: " + t.getMessage());
                if (!silent) {
                    Toast.makeText(QuanLyDanhGia.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEmptyState() {
        if (reviewList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvDanhGia.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvDanhGia.setVisibility(View.VISIBLE);
        }
    }

    private void showChiTietDanhGiaDialog(Review review) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chi_tiet_danh_gia);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Ánh xạ views
        TextView tvOrderId = dialog.findViewById(R.id.tvOrderId);
        TextView tvOrderStatus = dialog.findViewById(R.id.tvOrderStatus);
        TextView tvTotalPrice = dialog.findViewById(R.id.tvTotalPrice);
        TextView tvUserName = dialog.findViewById(R.id.tvUserName);
        TextView tvUserEmail = dialog.findViewById(R.id.tvUserEmail);
        TextView tvUserPhone = dialog.findViewById(R.id.tvUserPhone);
        TextView tvReceiverName = dialog.findViewById(R.id.tvReceiverName);
        TextView tvReceiverAddress = dialog.findViewById(R.id.tvReceiverAddress);
        TextView tvReceiverPhone = dialog.findViewById(R.id.tvReceiverPhone);
        RecyclerView rvOrderDetails = dialog.findViewById(R.id.rvOrderDetails);
        ImageView star1 = dialog.findViewById(R.id.star1);
        ImageView star2 = dialog.findViewById(R.id.star2);
        ImageView star3 = dialog.findViewById(R.id.star3);
        ImageView star4 = dialog.findViewById(R.id.star4);
        ImageView star5 = dialog.findViewById(R.id.star5);
        TextView tvComment = dialog.findViewById(R.id.tvComment);
        Button btnDong = dialog.findViewById(R.id.btnDong);

        // Hiển thị mã đơn hàng (order_id như DA1, DA2, không phải _id)
        String orderIdText = review.getOrderId() != null ? review.getOrderId() : "N/A";
        // Nếu orderId là _id (dài), cần query để lấy order_id thực
        if (orderIdText.length() > 10) {
            // Có thể là _id, sẽ cập nhật sau khi load order
            tvOrderId.setText("Mã đơn: Đang tải...");
        } else {
            tvOrderId.setText("Mã đơn: " + orderIdText);
        }

        // Hiển thị rating
        int rating = review.getRating();
        updateStars(star1, star2, star3, star4, star5, rating);

        // Hiển thị comment
        String comment = review.getComment();
        if (comment == null || comment.trim().isEmpty()) {
            tvComment.setText("Không có bình luận");
        } else {
            tvComment.setText(comment);
        }

        // Hiển thị thông tin user (từ populated data)
        tvUserName.setText("Tên: " + (review.getUserName() != null ? review.getUserName() : "N/A"));
        tvUserEmail.setText("Email: " + (review.getUserEmail() != null ? review.getUserEmail() : "N/A"));
        tvUserPhone.setText("SĐT: " + (review.getUserPhone() != null ? review.getUserPhone() : "N/A"));

        // Load order details và order info
        // orderObjectId là _id của order để query
        String orderObjectId = review.getOrderObjectId();
        if (orderObjectId == null || orderObjectId.isEmpty()) {
            // Nếu không có orderObjectId, thử dùng orderId (có thể là _id)
            orderObjectId = review.getOrderId();
        }
        
        Log.d("QuanLyDanhGia", "Order Object ID: " + orderObjectId + ", Order ID (mã đơn): " + review.getOrderId());
        
        if (orderObjectId != null && !orderObjectId.isEmpty() && orderObjectId.length() > 10) {
            // orderObjectId là _id (dài), dùng để query
            loadOrderInfo(orderObjectId, tvOrderStatus, tvTotalPrice, tvReceiverName, tvReceiverAddress, tvReceiverPhone, tvOrderId);
            loadOrderDetails(orderObjectId, rvOrderDetails);
        } else if (review.getOrderId() != null && review.getOrderId().length() <= 10) {
            // orderId là mã đơn (DA1, DA2), tìm order theo mã đơn
            findOrderByOrderId(review.getOrderId(), tvOrderStatus, tvTotalPrice, tvReceiverName, tvReceiverAddress, tvReceiverPhone, tvOrderId, rvOrderDetails);
        } else {
            Log.e("QuanLyDanhGia", "OrderObjectId is null or empty, orderId: " + review.getOrderId());
            // Thử tìm order theo orderId nếu có
            if (review.getOrderId() != null) {
                findOrderByOrderId(review.getOrderId(), tvOrderStatus, tvTotalPrice, tvReceiverName, tvReceiverAddress, tvReceiverPhone, tvOrderId, rvOrderDetails);
            } else {
                Toast.makeText(this, "Không thể tải thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            }
        }

        // Xử lý nút đóng
        btnDong.setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        // Cho phép đóng dialog khi click ra ngoài
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        
        dialog.show();
    }

    private void updateStars(ImageView star1, ImageView star2, ImageView star3, ImageView star4, ImageView star5, int rating) {
        star1.setImageResource(rating >= 1 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        star2.setImageResource(rating >= 2 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        star3.setImageResource(rating >= 3 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        star4.setImageResource(rating >= 4 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        star5.setImageResource(rating >= 5 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
    }

    private void loadOrderInfo(String orderId, TextView tvStatus, TextView tvTotalPrice, 
                               TextView tvReceiverName, TextView tvReceiverAddress, TextView tvReceiverPhone, TextView tvOrderId) {
        Log.d("QuanLyDanhGia", "Loading order info for ID: " + orderId);
        apiServices.getOrderById(orderId).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(Call<Response<Order>> call, retrofit2.Response<Response<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Order> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        Order order = res.getData();
                        
                        // Hiển thị mã đơn hàng (order_id như DA1, DA2)
                        String orderIdText = order.getOrderId() != null ? order.getOrderId() : 
                                            (order.getId() != null ? order.getId().substring(order.getId().length() - 6) : "N/A");
                        tvOrderId.setText("Mã đơn: " + orderIdText);
                        
                        // Hiển thị thông tin order
                        String status = order.getStatus() != null ? order.getStatus() : "Chưa xác định";
                        tvStatus.setText("Trạng thái: " + status);
                        
                        double totalPrice = order.getTotalPrice();
                        long totalLong = Math.round(totalPrice);
                        // Format số với dấu chấm phân cách hàng nghìn
                        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                        String formattedTotal = df.format(totalLong).replace(",", ".") + "đ";
                        tvTotalPrice.setText("Tổng tiền: " + formattedTotal);
                        
                        tvReceiverName.setText("Tên: " + (order.getReceiverName() != null ? order.getReceiverName() : "N/A"));
                        tvReceiverAddress.setText("Địa chỉ: " + (order.getReceiverAddress() != null ? order.getReceiverAddress() : "N/A"));
                        tvReceiverPhone.setText("SĐT: " + (order.getReceiverPhone() != null ? order.getReceiverPhone() : "N/A"));
                        
                        Log.d("QuanLyDanhGia", "Order info loaded successfully");
                    } else {
                        Log.e("QuanLyDanhGia", "Load order info failed: " + (res.getMessage() != null ? res.getMessage() : "Unknown error"));
                    }
                } else {
                    String errorMsg = "Load order info failed";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("QuanLyDanhGia", "Load order info failed: " + errorMsg + ", Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Response<Order>> call, Throwable t) {
                Log.e("QuanLyDanhGia", "Load order info failure: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void findOrderByOrderId(String orderIdText, TextView tvStatus, TextView tvTotalPrice, 
                                    TextView tvReceiverName, TextView tvReceiverAddress, TextView tvReceiverPhone, 
                                    TextView tvOrderId, RecyclerView rvOrderDetails) {
        // Tìm order theo order_id (mã đơn như DA1, DA2)
        apiServices.getAllOrders().enqueue(new Callback<Response<List<Order>>>() {
            @Override
            public void onResponse(Call<Response<List<Order>>> call, retrofit2.Response<Response<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Order>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        // Tìm order theo order_id
                        boolean found = false;
                        for (Order order : res.getData()) {
                            if (order.getOrderId() != null && order.getOrderId().equals(orderIdText)) {
                                found = true;
                                // Hiển thị mã đơn hàng (order_id như DA1, DA2)
                                tvOrderId.setText("Mã đơn: " + orderIdText);
                                
                                // Tìm thấy order, hiển thị thông tin
                                String status = order.getStatus() != null ? order.getStatus() : "Chưa xác định";
                                tvStatus.setText("Trạng thái: " + status);
                                
                                double totalPrice = order.getTotalPrice();
                                long totalLong = Math.round(totalPrice);
                                // Format số với dấu chấm phân cách hàng nghìn
                                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                                String formattedTotal = df.format(totalLong).replace(",", ".") + "đ";
                                tvTotalPrice.setText("Tổng tiền: " + formattedTotal);
                                
                                tvReceiverName.setText("Tên: " + (order.getReceiverName() != null ? order.getReceiverName() : "N/A"));
                                tvReceiverAddress.setText("Địa chỉ: " + (order.getReceiverAddress() != null ? order.getReceiverAddress() : "N/A"));
                                tvReceiverPhone.setText("SĐT: " + (order.getReceiverPhone() != null ? order.getReceiverPhone() : "N/A"));
                                
                                // Load order details
                                loadOrderDetails(order.getId(), rvOrderDetails);
                                break;
                            }
                        }
                        if (!found) {
                            Log.e("QuanLyDanhGia", "Order not found with order_id: " + orderIdText);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<List<Order>>> call, Throwable t) {
                Log.e("QuanLyDanhGia", "Find order by order_id failure: " + t.getMessage());
            }
        });
    }

    private void loadOrderDetails(String orderId, RecyclerView rvOrderDetails) {
        OrderDetailAdapter orderDetailAdapter = new OrderDetailAdapter(this, new ArrayList<>());
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        rvOrderDetails.setAdapter(orderDetailAdapter);

        apiServices.getOrderDetails(orderId).enqueue(new Callback<Response<List<OrderDetail>>>() {
            @Override
            public void onResponse(Call<Response<List<OrderDetail>>> call, retrofit2.Response<Response<List<OrderDetail>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<OrderDetail>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        orderDetailAdapter.updateList(res.getData());
                        Log.d("QuanLyDanhGia", "Loaded " + res.getData().size() + " order details");
                    }
                } else {
                    Log.e("QuanLyDanhGia", "Load order details failed");
                }
            }

            @Override
            public void onFailure(Call<Response<List<OrderDetail>>> call, Throwable t) {
                Log.e("QuanLyDanhGia", "Load order details failure: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
