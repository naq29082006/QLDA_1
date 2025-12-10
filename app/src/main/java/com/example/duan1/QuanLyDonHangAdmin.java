package com.example.duan1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.duan1.model.User;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.PollingHelper;
import com.example.duan1.utils.RetrofitClient;
import com.example.duan1.OrderAdapter;
import com.example.duan1.OrderDetailAdapter;
import com.example.duan1.OrderStatusAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class QuanLyDonHangAdmin extends AppCompatActivity {
    private RecyclerView rvDonHang, rvStatusButtons;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imgBack;
    private ApiServices apiServices;
    private List<Order> orderList;
    private OrderAdapter orderAdapter;
    private OrderStatusAdapter statusAdapter;
    private String selectedStatus = "chờ xác nhận"; // Trạng thái được chọn
    private PollingHelper pollingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_don_hang_admin);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ views
        rvDonHang = findViewById(R.id.rvDonHang);
        rvStatusButtons = findViewById(R.id.rvStatusButtons);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        imgBack = findViewById(R.id.imgBack);
        
        // Xử lý nút back
        imgBack.setOnClickListener(v -> finish());

        // Khởi tạo danh sách
        orderList = new ArrayList<>();

        // Setup RecyclerView cho status buttons (horizontal)
        statusAdapter = new OrderStatusAdapter(this);
        rvStatusButtons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvStatusButtons.setAdapter(statusAdapter);
        
        // Xử lý click vào status button
        statusAdapter.setOnStatusClickListener(status -> {
            selectedStatus = status;
            loadOrdersByStatus(status, false);
        });

        // Setup RecyclerView cho đơn hàng
        orderAdapter = new OrderAdapter(this, orderList);
        orderAdapter.setOrderStatusFilter(selectedStatus);
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
                new android.app.AlertDialog.Builder(QuanLyDonHangAdmin.this)
                        .setTitle("Xác nhận đơn hàng")
                        .setMessage("Bạn có chắc chắn muốn xác nhận đơn hàng này?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            updateOrderStatus(order.getId(), "đang chuẩn bị", position, null);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onHuyClick(Order order, int position) {
                // Admin hủy khi đơn ở trạng thái "chờ xác nhận"
                String currentStatus = order.getStatus() != null ? order.getStatus().toLowerCase() : "";
                if (currentStatus.contains("chờ xác nhận") || currentStatus.contains("pending")) {
                    showCancelReasonDialog(order, position);
                } else {
                    // Nếu đang giao thì không cho admin hủy, chỉ user mới hủy được
                    Toast.makeText(QuanLyDonHangAdmin.this, "Không thể hủy đơn hàng đang giao", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBatDauGiaoClick(Order order, int position) {
                new android.app.AlertDialog.Builder(QuanLyDonHangAdmin.this)
                        .setTitle("Xác nhận bắt đầu giao")
                        .setMessage("Bạn có chắc chắn muốn bắt đầu giao đơn hàng này?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            updateOrderStatus(order.getId(), "đang giao", position, null);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onGiaoThanhCongClick(Order order, int position) {
                new android.app.AlertDialog.Builder(QuanLyDonHangAdmin.this)
                        .setTitle("Xác nhận giao thành công")
                        .setMessage("Bạn có chắc chắn đơn hàng đã được giao thành công?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            updateOrderStatus(order.getId(), "đã giao", position, null);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onGiaoThatBaiClick(Order order, int position) {
                // Khi giao thất bại, cho phép user hủy
                updateOrderStatus(order.getId(), "user đã hủy", position, null);
            }

            @Override
            public void onDanhGiaClick(Order order, int position) {
                // Không cần cho admin
            }
        });

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadOrdersByStatus(selectedStatus, false);
        });
        
        // Khởi tạo PollingHelper để tự động cập nhật mỗi 5 giây
        pollingHelper = new PollingHelper("QuanLyDonHangAdmin", 5000);
        pollingHelper.setRefreshCallback(() -> {
            loadOrdersByStatus(selectedStatus, true); // Silent refresh
        });
        pollingHelper.startPolling();

        // Load đơn hàng mặc định (chờ xác nhận)
        loadOrdersByStatus(selectedStatus, false);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollingHelper != null) {
            pollingHelper.stopPolling();
        }
    }

    private void loadOrdersByStatus(String status) {
        loadOrdersByStatus(status, false);
    }
    
    private void loadOrdersByStatus(String status, boolean silent) {
        // Set filter cho adapter
        orderAdapter.setOrderStatusFilter(status);
        
        // Load tất cả đơn hàng và filter theo status
        apiServices.getAllOrders().enqueue(new Callback<Response<List<Order>>>() {
            @Override
            public void onResponse(Call<Response<List<Order>>> call, retrofit2.Response<Response<List<Order>>> response) {
                // Dừng refresh indicator
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Order>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        // Filter đơn hàng theo status
                        List<Order> newOrderList = new ArrayList<>();
                        for (Order order : res.getData()) {
                            if (matchesStatus(order.getStatus(), status)) {
                                newOrderList.add(order);
                            }
                        }
                        
                        // So sánh với danh sách cũ để phát hiện thay đổi
                        boolean hasChanges = hasOrderListChanged(orderList, newOrderList);
                        if (hasChanges) {
                            orderList.clear();
                            orderList.addAll(newOrderList);
                            orderAdapter.updateList(orderList);
                            updateEmptyState();
                            
                            if (!silent) {
                                Log.d("QuanLyDonHangAdmin", "Loaded " + orderList.size() + " orders with status: " + status);
                            } else {
                                Log.d("QuanLyDonHangAdmin", "Silent refresh: " + orderList.size() + " orders");
                            }
                        } else if (!silent) {
                            // Không có thay đổi nhưng vẫn update để đảm bảo UI sync
                            orderList.clear();
                            orderList.addAll(newOrderList);
                            orderAdapter.updateList(orderList);
                            updateEmptyState();
                        }
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
                    Log.e("QuanLyDonHangAdmin", "Load orders failed: " + errorMsg);
                    if (!silent) {
                        Toast.makeText(QuanLyDonHangAdmin.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<List<Order>>> call, Throwable t) {
                // Dừng refresh indicator
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                Log.e("QuanLyDonHangAdmin", "Load orders failure: " + t.getMessage());
                if (!silent) {
                    Toast.makeText(QuanLyDonHangAdmin.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private boolean matchesStatus(String orderStatus, String filterStatus) {
        if (orderStatus == null || filterStatus == null) return false;
        
        orderStatus = orderStatus.toLowerCase();
        filterStatus = filterStatus.toLowerCase();
        
        switch (filterStatus) {
            case "chờ xác nhận":
                return orderStatus.contains("chờ xác nhận") || 
                       orderStatus.contains("pending") || 
                       orderStatus.contains("đang chờ");
            case "đang chuẩn bị":
                return orderStatus.contains("đang chuẩn bị") || 
                       orderStatus.contains("đang chuẩn bị đơn hàng") ||
                       orderStatus.contains("chờ lấy hàng") || 
                       orderStatus.contains("preparing");
            case "đang giao":
                return orderStatus.contains("đang giao") || 
                       orderStatus.contains("delivering");
            case "hoàn thành":
                return orderStatus.contains("đã nhận") || 
                       orderStatus.contains("đã giao") || 
                       orderStatus.contains("delivered") ||
                       orderStatus.contains("người dùng hủy") ||
                       orderStatus.contains("user đã hủy") ||
                       orderStatus.contains("admin đã hủy") ||
                       orderStatus.contains("đã hủy") ||
                       orderStatus.contains("cancelled") ||
                       orderStatus.contains("giao thành công");
            default:
                return orderStatus.contains(filterStatus);
        }
    }

    private boolean hasOrderListChanged(List<Order> oldList, List<Order> newList) {
        if (oldList.size() != newList.size()) {
            return true;
        }
        
        // So sánh từng đơn hàng theo ID và status
        for (int i = 0; i < oldList.size(); i++) {
            Order oldOrder = oldList.get(i);
            Order newOrder = newList.get(i);
            
            if (oldOrder == null || newOrder == null) {
                return true;
            }
            
            String oldId = oldOrder.getId();
            String newId = newOrder.getId();
            String oldStatus = oldOrder.getStatus();
            String newStatus = newOrder.getStatus();
            
            if (oldId == null || newId == null || !oldId.equals(newId)) {
                return true;
            }
            
            if (oldStatus == null || newStatus == null || !oldStatus.equals(newStatus)) {
                return true;
            }
        }
        
        return false;
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
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
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

        // Ẩn nút hủy đơn hàng cho admin
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
        android.util.Log.d("QuanLyDonHangAdmin", "Order createdAt: " + createdAtStr);
        
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
                        android.util.Log.e("QuanLyDonHangAdmin", "Error parsing createdAt: " + createdAtStr, e3);
                        tvOrderDate.setText(createdAtStr); // Hiển thị raw string nếu không parse được
                    }
                }
            }
        } else {
            android.util.Log.w("QuanLyDonHangAdmin", "Order createdAt is null or empty for order: " + order.getOrderId());
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
                Log.e("QuanLyDonHangAdmin", "Load user info failure: " + t.getMessage());
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
                        Log.d("QuanLyDonHangAdmin", "Loaded " + res.getData().size() + " order details");
                    }
                } else {
                    Log.e("QuanLyDonHangAdmin", "Load order details failed");
                }
            }

            @Override
            public void onFailure(Call<Response<List<OrderDetail>>> call, Throwable t) {
                Log.e("QuanLyDonHangAdmin", "Load order details failure: " + t.getMessage());
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

    private void showCancelReasonDialog(Order order, int position) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ly_do_huy);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.EditText edtLyDo = dialog.findViewById(R.id.edtLyDo);
        android.widget.Button btnXacNhan = dialog.findViewById(R.id.btnXacNhan);
        android.widget.Button btnHuy = dialog.findViewById(R.id.btnHuy);

        btnXacNhan.setOnClickListener(v -> {
            String lyDo = edtLyDo.getText().toString().trim();
            if (lyDo.isEmpty() || lyDo.length() < 5) {
                Toast.makeText(this, "Vui lòng nhập lý do hủy (ít nhất 5 ký tự)", Toast.LENGTH_SHORT).show();
                return;
            }
            updateOrderStatus(order.getId(), "admin đã hủy", position, lyDo);
            dialog.dismiss();
        });

        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateOrderStatus(String orderId, String newStatus, int position, String cancelReason) {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("status", newStatus);
        if (cancelReason != null && !cancelReason.isEmpty()) {
            body.put("cancel_reason", cancelReason);
        }

        apiServices.updateOrderStatus(orderId, body).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(Call<Response<Order>> call, retrofit2.Response<Response<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Order> res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(QuanLyDonHangAdmin.this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                        // Reload orders
                        loadOrdersByStatus(selectedStatus, false);
                    } else {
                        Toast.makeText(QuanLyDonHangAdmin.this, res.getMessage() != null ? res.getMessage() : "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QuanLyDonHangAdmin.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Order>> call, Throwable t) {
                Log.e("QuanLyDonHangAdmin", "Update order status failure: " + t.getMessage());
                Toast.makeText(QuanLyDonHangAdmin.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

