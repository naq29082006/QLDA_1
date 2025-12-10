package com.example.duan1;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.Order;
import com.example.duan1.model.OrderDetail;
import com.example.duan1.model.Response;
import com.example.duan1.model.User;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.RetrofitClient;
import com.example.duan1.OrderAdapter;
import com.example.duan1.OrderDetailAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class QuanLyDonHang extends AppCompatActivity {
    private RecyclerView rvDonHang;
    private TextView tvEmpty, tvTitle;
    private ApiServices apiServices;
    private List<Order> orderList;
    private OrderAdapter orderAdapter;
    private String orderStatus; // Trạng thái đơn hàng cần hiển thị

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_don_hang);

        // Lấy trạng thái từ intent
        orderStatus = getIntent().getStringExtra("order_status");
        if (orderStatus == null) {
            orderStatus = "chờ xác nhận";
        }

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ views
        rvDonHang = findViewById(R.id.rvDonHang);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTitle = findViewById(R.id.tvTitle);

        // Set title theo trạng thái
        String title = getTitleByStatus(orderStatus);
        tvTitle.setText(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo danh sách
        orderList = new ArrayList<>();

        // Setup RecyclerView
        orderAdapter = new OrderAdapter(this, orderList);
        orderAdapter.setOrderStatusFilter(orderStatus);
        rvDonHang.setLayoutManager(new LinearLayoutManager(this));
        rvDonHang.setAdapter(orderAdapter);

        // Xử lý click vào đơn hàng để xem chi tiết
        orderAdapter.setOnDetailClickListener(order -> {
            showChiTietDonHangDialog(order);
        });

        // Xử lý các action buttons
        orderAdapter.setOnActionClickListener(new OrderAdapter.OnActionClickListener() {
            @Override
            public void onXacNhanClick(Order order, int position) {
                updateOrderStatus(order.getId(), "đang chuẩn bị", position);
            }

            @Override
            public void onHuyClick(Order order, int position) {
                updateOrderStatus(order.getId(), "admin đã hủy", position);
            }

            @Override
            public void onBatDauGiaoClick(Order order, int position) {
                updateOrderStatus(order.getId(), "đang giao", position);
            }

            @Override
            public void onGiaoThanhCongClick(Order order, int position) {
                updateOrderStatus(order.getId(), "đã giao", position);
            }

            @Override
            public void onGiaoThatBaiClick(Order order, int position) {
                updateOrderStatus(order.getId(), "giao thất bại", position);
            }

            @Override
            public void onDanhGiaClick(Order order, int position) {
                // Không cần cho admin
            }
        });

        // Load đơn hàng
        loadOrders();
    }

    private String getTitleByStatus(String status) {
        if (status == null) return "Quản lý đơn hàng";
        switch (status.toLowerCase()) {
            case "chờ xác nhận":
            case "pending":
            case "đang chờ":
                return "Đơn hàng chờ xác nhận";
            case "chờ lấy hàng":
            case "đang chuẩn bị":
            case "đang chuẩn bị đơn hàng":
            case "preparing":
                return "Đơn hàng đang chuẩn bị";
            case "đang giao":
            case "delivering":
                return "Đơn hàng đang giao";
            case "đã giao":
            case "đã nhận":
            case "delivered":
                return "Đơn hàng hoàn thành";
            default:
                return "Quản lý đơn hàng";
        }
    }

    private void loadOrders() {
        apiServices.getAllOrders().enqueue(new Callback<Response<List<Order>>>() {
            @Override
            public void onResponse(Call<Response<List<Order>>> call, retrofit2.Response<Response<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Order>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        // Lọc đơn hàng theo trạng thái
                        orderList.clear();
                        for (Order order : res.getData()) {
                            String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "";
                            String filterStatus = orderStatus != null ? orderStatus.toLowerCase() : "";
                            
                            if (matchesStatus(status, filterStatus)) {
                                orderList.add(order);
                            }
                        }
                        orderAdapter.updateList(orderList);
                        updateEmptyState();
                        Log.d("QuanLyDonHang", "Loaded " + orderList.size() + " orders with status: " + orderStatus);
                    } else {
                        orderList.clear();
                        orderAdapter.updateList(orderList);
                        updateEmptyState();
                    }
                } else {
                    String errorMsg = "Lỗi khi tải đơn hàng";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("QuanLyDonHang", "Load orders failed: " + errorMsg);
                    Toast.makeText(QuanLyDonHang.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<List<Order>>> call, Throwable t) {
                Log.e("QuanLyDonHang", "Load orders failure: " + t.getMessage());
                Toast.makeText(QuanLyDonHang.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean matchesStatus(String orderStatus, String filterStatus) {
        if (filterStatus == null || filterStatus.isEmpty()) return true;
        
        orderStatus = orderStatus.toLowerCase();
        filterStatus = filterStatus.toLowerCase();
        
        switch (filterStatus) {
            case "chờ xác nhận":
            case "pending":
            case "đang chờ":
                return orderStatus.contains("chờ xác nhận") || 
                       orderStatus.contains("pending") || 
                       orderStatus.contains("đang chờ");
            case "chờ lấy hàng":
            case "đang chuẩn bị":
            case "đang chuẩn bị đơn hàng":
            case "preparing":
                return orderStatus.contains("chờ lấy hàng") || 
                       orderStatus.contains("đang chuẩn bị") || 
                       orderStatus.contains("đang chuẩn bị đơn hàng") ||
                       orderStatus.contains("preparing");
            case "đang giao":
            case "delivering":
                return orderStatus.contains("đang giao") || 
                       orderStatus.contains("delivering");
            case "đã giao":
            case "đã nhận":
            case "delivered":
            case "hoàn thành":
                return orderStatus.contains("đã giao") || 
                       orderStatus.contains("đã nhận") || 
                       orderStatus.contains("delivered") ||
                       orderStatus.contains("người dùng hủy") ||
                       orderStatus.contains("user đã hủy") ||
                       orderStatus.contains("admin đã hủy") ||
                       orderStatus.contains("đã hủy") ||
                       orderStatus.contains("cancelled");
            default:
                return orderStatus.contains(filterStatus);
        }
    }

    private void updateOrderStatus(String orderId, String newStatus, int position) {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", newStatus);

        apiServices.updateOrderStatus(orderId, body).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(Call<Response<Order>> call, retrofit2.Response<Response<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Order> res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(QuanLyDonHang.this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                        // Reload orders
                        loadOrders();
                    } else {
                        Toast.makeText(QuanLyDonHang.this, res.getMessage() != null ? res.getMessage() : "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QuanLyDonHang.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Order>> call, Throwable t) {
                Log.e("QuanLyDonHang", "Update order status failure: " + t.getMessage());
                Toast.makeText(QuanLyDonHang.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvDonHang.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvDonHang.setVisibility(View.VISIBLE);
        }
    }

    private void showChiTietDonHangDialog(Order order) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chi_tiet_don_hang);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvOrderId = dialog.findViewById(R.id.tvOrderId);
        TextView tvStatus = dialog.findViewById(R.id.tvStatus);
        TextView tvOrderDate = dialog.findViewById(R.id.tvOrderDate);
        TextView tvTotalPrice = dialog.findViewById(R.id.tvTotalPrice);
        TextView tvSubtotal = dialog.findViewById(R.id.tvSubtotal);
        View layoutVoucher = dialog.findViewById(R.id.layoutVoucher);
        TextView tvVoucherTitle = dialog.findViewById(R.id.tvVoucherTitle);
        TextView tvVoucherCode = dialog.findViewById(R.id.tvVoucherCode);
        TextView tvVoucherDiscount = dialog.findViewById(R.id.tvVoucherDiscount);
        TextView tvUserName = dialog.findViewById(R.id.tvUserName);
        TextView tvUserEmail = dialog.findViewById(R.id.tvUserEmail);
        TextView tvUserPhone = dialog.findViewById(R.id.tvUserPhone);
        TextView tvReceiverName = dialog.findViewById(R.id.tvReceiverName);
        TextView tvReceiverAddress = dialog.findViewById(R.id.tvReceiverAddress);
        TextView tvReceiverPhone = dialog.findViewById(R.id.tvReceiverPhone);
        View layoutCancelReason = dialog.findViewById(R.id.layoutCancelReason);
        TextView tvCancelReason = dialog.findViewById(R.id.tvCancelReason);
        RecyclerView rvOrderDetails = dialog.findViewById(R.id.rvOrderDetails);
        Button btnHuyDonHang = dialog.findViewById(R.id.btnHuyDonHang);
        Button btnDong = dialog.findViewById(R.id.btnDong);

        // Ẩn nút hủy đơn hàng cho admin (admin không hủy từ dialog này)
        btnHuyDonHang.setVisibility(View.GONE);

        // Hiển thị lý do hủy nếu có
        if (order.getCancelReason() != null && !order.getCancelReason().isEmpty()) {
            layoutCancelReason.setVisibility(View.VISIBLE);
            tvCancelReason.setText(order.getCancelReason());
        } else {
            layoutCancelReason.setVisibility(View.GONE);
        }

        // Hiển thị thông tin
        String orderIdText = order.getOrderId() != null ? order.getOrderId() : 
                            (order.getId() != null ? order.getId().substring(order.getId().length() - 6) : "N/A");
        tvOrderId.setText(orderIdText);
        
        String status = order.getStatus() != null ? order.getStatus() : "Chưa xác định";
        tvStatus.setText(status);
        tvStatus.setTextColor(order.getStatusColor());
        
        // Hiển thị thời gian đặt hàng
        String createdAtStr = order.getCreatedAt();
        android.util.Log.d("QuanLyDonHang", "Order createdAt: " + createdAtStr);
        
        if (createdAtStr != null && !createdAtStr.isEmpty()) {
            try {
                // Parse ISO format từ server (yyyy-MM-dd'T'HH:mm:ss.SSS'Z' hoặc yyyy-MM-dd'T'HH:mm:ss'Z')
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date date = inputFormat.parse(createdAtStr);
                
                // Format lại thành dd/MM/yyyy HH:mm
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                tvOrderDate.setText(outputFormat.format(date));
            } catch (Exception e) {
                // Thử parse format khác (không có milliseconds)
                try {
                    java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                    inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    java.util.Date date = inputFormat.parse(createdAtStr);
                    java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                    tvOrderDate.setText(outputFormat.format(date));
                } catch (Exception e2) {
                    // Thử parse format không có Z
                    try {
                        java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                        java.util.Date date = inputFormat.parse(createdAtStr);
                        java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                        tvOrderDate.setText(outputFormat.format(date));
                    } catch (Exception e3) {
                        android.util.Log.e("QuanLyDonHang", "Error parsing createdAt: " + createdAtStr, e3);
                        tvOrderDate.setText(createdAtStr); // Hiển thị raw string nếu không parse được
                    }
                }
            }
        } else {
            android.util.Log.w("QuanLyDonHang", "Order createdAt is null or empty for order: " + order.getOrderId());
            tvOrderDate.setText("N/A");
        }

        // Hiển thị subtotal
        double subtotal = order.getSubtotal();
        long subtotalLong = Math.round(subtotal);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        String formattedSubtotal = df.format(subtotalLong).replace(",", ".") + "đ";
        tvSubtotal.setText(formattedSubtotal);

        // Hiển thị voucher nếu có - lấy thông tin từ API để hiển thị max discount
        if (order.getVoucherCode() != null && !order.getVoucherCode().isEmpty()) {
            layoutVoucher.setVisibility(View.VISIBLE);
            tvVoucherTitle.setText(order.getVoucherTitle() != null ? order.getVoucherTitle() : "Voucher");
            tvVoucherCode.setText(order.getVoucherCode());
            double discount = order.getDiscountAmount();
            long discountLong = Math.round(discount);
            String formattedDiscount = df.format(discountLong).replace(",", ".") + "đ";
            tvVoucherDiscount.setText("-" + formattedDiscount);
            
            // Lấy thông tin voucher từ API để hiển thị max discount
            loadVoucherInfoForOrder(order.getVoucherCode(), order.getDiscountAmount(), tvVoucherDiscount);
        } else {
            layoutVoucher.setVisibility(View.GONE);
        }

        // Hiển thị thành tiền (sau khi giảm giá)
        double totalPrice = order.getTotalPrice();
        long totalLong = Math.round(totalPrice);
        String formattedTotal = df.format(totalLong).replace(",", ".") + "đ";
        tvTotalPrice.setText(formattedTotal);

        tvReceiverName.setText(order.getReceiverName() != null ? order.getReceiverName() : "N/A");
        tvReceiverAddress.setText(order.getReceiverAddress() != null ? order.getReceiverAddress() : "N/A");
        tvReceiverPhone.setText(order.getReceiverPhone() != null ? order.getReceiverPhone() : "N/A");

        // Load thông tin người đặt
        loadUserInfo(order.getUserId(), tvUserName, tvUserEmail, tvUserPhone);

        // Setup RecyclerView cho danh sách sản phẩm
        OrderDetailAdapter orderDetailAdapter = new OrderDetailAdapter(this, new ArrayList<>());
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        rvOrderDetails.setAdapter(orderDetailAdapter);

        // Load order details
        loadOrderDetails(order.getId(), orderDetailAdapter);

        btnDong.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void loadUserInfo(String userId, TextView tvName, TextView tvEmail, TextView tvPhone) {
        if (userId == null || userId.isEmpty()) {
            tvName.setText("N/A");
            tvEmail.setText("N/A");
            tvPhone.setText("N/A");
            return;
        }
        
        // Tìm user trong danh sách users hoặc gọi API
        apiServices.getAllUsers().enqueue(new Callback<Response<List<User>>>() {
            @Override
            public void onResponse(Call<Response<List<User>>> call, retrofit2.Response<Response<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<User>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        for (User user : res.getData()) {
                            if (user.getId() != null && user.getId().equals(userId)) {
                                tvName.setText(user.getName() != null ? user.getName() : "N/A");
                                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
                                tvPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
                                return;
                            }
                        }
                    }
                }
                // Nếu không tìm thấy
                tvName.setText("N/A");
                tvEmail.setText("N/A");
                tvPhone.setText("N/A");
            }

            @Override
            public void onFailure(Call<Response<List<User>>> call, Throwable t) {
                Log.e("QuanLyDonHang", "Load user info failure: " + t.getMessage());
                tvName.setText("N/A");
                tvEmail.setText("N/A");
                tvPhone.setText("N/A");
            }

        });
    }

    private void loadOrderDetails(String orderId, OrderDetailAdapter adapter) {
        apiServices.getOrderDetails(orderId).enqueue(new Callback<Response<List<OrderDetail>>>() {
            @Override
            public void onResponse(Call<Response<List<OrderDetail>>> call, retrofit2.Response<Response<List<OrderDetail>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<OrderDetail>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        adapter.updateList(res.getData());
                        Log.d("QuanLyDonHang", "Loaded " + res.getData().size() + " order details");
                    }
                } else {
                    Log.e("QuanLyDonHang", "Load order details failed");
                }
            }

            @Override
            public void onFailure(Call<Response<List<OrderDetail>>> call, Throwable t) {
                Log.e("QuanLyDonHang", "Load order details failure: " + t.getMessage());
            }
        });
    }

    private void loadVoucherInfoForOrder(String voucherCode, double discountAmount, TextView tvVoucherDiscount) {
        // Lấy mã voucher đầu tiên nếu có nhiều voucher
        String firstVoucherCode = voucherCode.split(",")[0].trim();
        
        apiServices.getAllVouchers().enqueue(new Callback<Response<List<com.example.duan1.model.Voucher>>>() {
            @Override
            public void onResponse(Call<Response<List<com.example.duan1.model.Voucher>>> call, retrofit2.Response<Response<List<com.example.duan1.model.Voucher>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<com.example.duan1.model.Voucher>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        for (com.example.duan1.model.Voucher voucher : res.getData()) {
                            if (voucher.getVoucherCode().equalsIgnoreCase(firstVoucherCode)) {
                                // Cập nhật hiển thị với thông tin max discount
                                long discountLong = Math.round(discountAmount);
                                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                                String formattedDiscount = df.format(discountLong).replace(",", ".") + "đ";
                                
                                if (voucher.getMaxDiscountAmount() > 0) {
                                    long maxAmount = Math.round(voucher.getMaxDiscountAmount());
                                    String formattedMax = df.format(maxAmount).replace(",", ".") + "đ";
                                    tvVoucherDiscount.setText("-" + formattedDiscount + " (tối đa " + formattedMax + ")");
                                } else {
                                    tvVoucherDiscount.setText("-" + formattedDiscount);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<List<com.example.duan1.model.Voucher>>> call, Throwable t) {
                // Không làm gì nếu lỗi
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

