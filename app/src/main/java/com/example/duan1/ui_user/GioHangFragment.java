package com.example.duan1.ui_user;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.AdapterUser.CartAdapter;
import com.example.duan1.R;
import com.example.duan1.model.CartItem;
import com.example.duan1.model.Order;
import com.example.duan1.model.Response;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.PollingHelper;
import com.example.duan1.utils.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class GioHangFragment extends Fragment {

    private RecyclerView rvGioHang;
    private TextView tvEmpty, tvTotal, tvDiscount, tvFinalTotal, tvVoucherInfo;
    private View layoutVoucherInfo;
    private Button btnDatHang;
    private ApiServices apiServices;
    private List<CartItem> cartItemList;
    private CartAdapter cartAdapter;
    private String userId;
    private PollingHelper pollingHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gio_hang, container, false);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Lấy user_id từ SharedPreferences (lưu từ Dangnhap.java)
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", getContext().MODE_PRIVATE);
        userId = prefs.getString("id_taikhoan", null);
        
        // Debug: Log tất cả keys trong SharedPreferences
        java.util.Map<String, ?> allPrefs = prefs.getAll();
        Log.d("SharedPreferences", "All keys: " + allPrefs.keySet().toString());
        Log.d("SharedPreferences", "id_taikhoan value: " + userId);
        
        if (userId == null || userId.isEmpty()) {
            Log.e("Error", "User ID not found in SharedPreferences. Available keys: " + allPrefs.keySet());
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
        }

        // Ánh xạ views
        rvGioHang = view.findViewById(R.id.rvGioHang);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvDiscount = view.findViewById(R.id.tvDiscount);
        tvFinalTotal = view.findViewById(R.id.tvFinalTotal);
        tvVoucherInfo = view.findViewById(R.id.tvVoucherInfo);
        layoutVoucherInfo = view.findViewById(R.id.layoutVoucherInfo);
        btnDatHang = view.findViewById(R.id.btnDatHang);

        // Khởi tạo danh sách
        cartItemList = new ArrayList<>();

        // Setup RecyclerView
        cartAdapter = new CartAdapter(getContext(), cartItemList);
        rvGioHang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGioHang.setAdapter(cartAdapter);

        // Xử lý thay đổi trong giỏ hàng
        cartAdapter.setOnCartItemChangeListener(new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onItemSelectedChanged() {
                updateTotal();
            }

            @Override
            public void onQuantityChanged(CartItem cartItem, int newQuantity) {
                updateCartItemQuantity(cartItem, newQuantity);
            }

            @Override
            public void onItemDeleted(CartItem cartItem) {
                deleteCartItem(cartItem);
            }
        });

        // Xử lý đặt hàng
        btnDatHang.setOnClickListener(v -> {
            List<CartItem> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            } else {
                showThongTinNguoiNhanDialog(selectedItems);
            }
        });

        // Load giỏ hàng
        if (userId != null) {
            loadCart();
            
            // Khởi tạo PollingHelper để tự động cập nhật mỗi 5 giây
            pollingHelper = new PollingHelper("GioHang", 5000);
            pollingHelper.setRefreshCallback(() -> {
                loadCart();
            });
            pollingHelper.startPolling();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pollingHelper != null) {
            pollingHelper.stopPolling();
        }
    }

    private void loadCart() {
        if (userId == null) return;

        apiServices.getCart(userId).enqueue(new Callback<Response<List<CartItem>>>() {
            @Override
            public void onResponse(Call<Response<List<CartItem>>> call, retrofit2.Response<Response<List<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<CartItem>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        cartItemList.clear();
                        cartItemList.addAll(res.getData());
                        // Product đã được parse bởi CartItemTypeAdapter
                        cartAdapter.updateList(cartItemList);
                        updateTotal();
                        updateEmptyState();
                        Log.d("API", "Loaded " + cartItemList.size() + " cart items");
                    } else {
                        cartItemList.clear();
                        cartAdapter.updateList(cartItemList);
                        updateEmptyState();
                    }
                } else {
                    Log.e("API Error", "Load cart failed: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<Response<List<CartItem>>> call, Throwable t) {
                Log.e("API Error", "Load cart failure: " + t.getMessage());
            }
        });
    }

    private void updateCartItemQuantity(CartItem cartItem, int newQuantity) {
        Map<String, String> body = new HashMap<>();
        body.put("quantity", String.valueOf(newQuantity));

        apiServices.updateCart(cartItem.getId(), body).enqueue(new Callback<Response<CartItem>>() {
            @Override
            public void onResponse(Call<Response<CartItem>> call, retrofit2.Response<Response<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<CartItem> res = response.body();
                    if (res.isSuccess()) {
                        loadCart(); // Reload cart
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<CartItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCartItem(CartItem cartItem) {
        apiServices.deleteCart(cartItem.getId()).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Void> res = response.body();
                    if (res.isSuccess()) {
                        loadCart(); // Reload cart
                        Toast.makeText(getContext(), "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotal() {
        double total = cartAdapter.getSelectedTotal();
        long totalLong = Math.round(total);
        // Format số với dấu chấm phân cách hàng nghìn
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        String formattedTotal = df.format(totalLong).replace(",", ".") + "đ";
        tvTotal.setText(formattedTotal);
        
        // Kiểm tra voucher đang active (voucher sẽ được áp dụng tự động khi đặt hàng)
        checkActiveVoucher(total);
    }
    
    private void checkActiveVoucher(double total) {
        // Lấy tất cả vouchers và tìm voucher đang active (check thời gian thực tế)
        apiServices.getAllVouchers().enqueue(new Callback<Response<List<com.example.duan1.model.Voucher>>>() {
            @Override
            public void onResponse(Call<Response<List<com.example.duan1.model.Voucher>>> call, retrofit2.Response<Response<List<com.example.duan1.model.Voucher>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<com.example.duan1.model.Voucher>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        List<com.example.duan1.model.Voucher> vouchers = res.getData();
                        List<com.example.duan1.model.Voucher> activeVouchers = new ArrayList<>();
                        
                        // Tìm TẤT CẢ voucher đang active (check thời gian thực tế)
                        java.util.Date now = new java.util.Date();
                        for (com.example.duan1.model.Voucher voucher : vouchers) {
                            if (voucher.isActive() && voucher.getStartDate() != null && voucher.getEndDate() != null) {
                                try {
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                                    
                                    String startDateStr = voucher.getStartDate();
                                    String endDateStr = voucher.getEndDate();
                                    
                                    java.util.Date startDate = null;
                                    java.util.Date endDate = null;
                                    
                                    // Thử parse ISO format trước
                                    try {
                                        startDate = sdf.parse(startDateStr);
                                        endDate = sdf.parse(endDateStr);
                                    } catch (Exception e1) {
                                        // Thử parse format yyyy-MM-dd
                                        try {
                                            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                                            startDate = dateFormat.parse(startDateStr);
                                            endDate = dateFormat.parse(endDateStr);
                                        } catch (Exception e2) {
                                            continue;
                                        }
                                    }
                                    
                                    if (startDate != null && endDate != null) {
                                        // Set end date to end of day
                                        java.util.Calendar cal = java.util.Calendar.getInstance();
                                        cal.setTime(endDate);
                                        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                                        cal.set(java.util.Calendar.MINUTE, 59);
                                        cal.set(java.util.Calendar.SECOND, 59);
                                        endDate = cal.getTime();
                                        
                                        if (now.compareTo(startDate) >= 0 && now.compareTo(endDate) <= 0) {
                                            activeVouchers.add(voucher);
                                        }
                                    }
                                } catch (Exception e) {
                                    continue;
                                }
                            }
                        }
                        
                        if (!activeVouchers.isEmpty()) {
                            // Tính tổng discount từ TẤT CẢ voucher
                            // Mỗi voucher được áp dụng riêng với max discount của nó
                            double totalDiscount = 0;
                            StringBuilder voucherInfoText = new StringBuilder();
                            
                            for (int i = 0; i < activeVouchers.size(); i++) {
                                com.example.duan1.model.Voucher voucher = activeVouchers.get(i);
                                
                                // Tính discount cho voucher này
                                double voucherDiscount = total * voucher.getDiscountPercentage() / 100.0;
                                
                                // Áp dụng max discount nếu có
                                if (voucher.getMaxDiscountAmount() > 0 && voucherDiscount > voucher.getMaxDiscountAmount()) {
                                    voucherDiscount = voucher.getMaxDiscountAmount();
                                }
                                
                                totalDiscount += voucherDiscount;
                                
                                if (i > 0) voucherInfoText.append("\n");
                                String title = voucher.getTitle() != null ? voucher.getTitle() : voucher.getVoucherCode();
                                voucherInfoText.append("• ").append(title).append(": -").append((int)voucher.getDiscountPercentage()).append("%");
                                if (voucher.getMaxDiscountAmount() > 0) {
                                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                                    long maxAmount = Math.round(voucher.getMaxDiscountAmount());
                                    voucherInfoText.append(" (tối đa ").append(df.format(maxAmount).replace(",", ".")).append("đ)");
                                }
                            }
                            
                            // Đảm bảo discount không vượt quá tổng tiền
                            totalDiscount = Math.min(totalDiscount, total);
                            
                            layoutVoucherInfo.setVisibility(View.VISIBLE);
                            tvVoucherInfo.setText(voucherInfoText.toString());
                            
                            updateFinalTotal(total, totalDiscount);
                        } else {
                            // Không có voucher
                            layoutVoucherInfo.setVisibility(View.GONE);
                            updateFinalTotal(total, 0);
                        }
                    } else {
                        layoutVoucherInfo.setVisibility(View.GONE);
                        updateFinalTotal(total, 0);
                    }
                } else {
                    layoutVoucherInfo.setVisibility(View.GONE);
                    updateFinalTotal(total, 0);
                }
            }

            @Override
            public void onFailure(Call<Response<List<com.example.duan1.model.Voucher>>> call, Throwable t) {
                layoutVoucherInfo.setVisibility(View.GONE);
                updateFinalTotal(total, 0);
            }
        });
    }
    
    private void updateFinalTotal(double total, double discount) {
        double finalTotal = Math.max(0, total - discount);
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        
        // Hiển thị giảm giá
        long discountLong = Math.round(discount);
        String formattedDiscount = df.format(discountLong).replace(",", ".") + "đ";
        tvDiscount.setText("-" + formattedDiscount);
        
        // Hiển thị thành tiền
        long finalTotalLong = Math.round(finalTotal);
        String formattedFinalTotal = df.format(finalTotalLong).replace(",", ".") + "đ";
        tvFinalTotal.setText(formattedFinalTotal);
    }

    private void updateEmptyState() {
        if (cartItemList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvGioHang.setVisibility(View.GONE);
            btnDatHang.setEnabled(false);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvGioHang.setVisibility(View.VISIBLE);
            btnDatHang.setEnabled(true);
        }
    }

    private void showThongTinNguoiNhanDialog(List<CartItem> selectedItems) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_thong_tin_nguoi_nhan);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputEditText edtReceiverName = dialog.findViewById(R.id.edtReceiverName);
        TextInputEditText edtReceiverAddress = dialog.findViewById(R.id.edtReceiverAddress);
        TextInputEditText edtReceiverPhone = dialog.findViewById(R.id.edtReceiverPhone);
        Button btnXacNhan = dialog.findViewById(R.id.btnXacNhan);
        Button btnHuy = dialog.findViewById(R.id.btnHuy);

        btnXacNhan.setOnClickListener(v -> {
            String name = edtReceiverName.getText().toString().trim();
            String address = edtReceiverAddress.getText().toString().trim();
            String phone = edtReceiverPhone.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate tên (ít nhất 2 ký tự)
            if (name.length() < 2) {
                Toast.makeText(getContext(), "Tên người nhận phải có ít nhất 2 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate địa chỉ (ít nhất 10 ký tự)
            if (address.length() < 10) {
                Toast.makeText(getContext(), "Địa chỉ phải có ít nhất 10 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate số điện thoại (phải có đúng 10 chữ số)
            String phoneDigits = phone.replaceAll("[^0-9]", "");
            if (phoneDigits.length() != 10) {
                Toast.makeText(getContext(), "Số điện thoại phải có đúng 10 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }

            // Confirm dialog
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Xác nhận đặt hàng")
                    .setMessage("Bạn có chắc chắn muốn đặt hàng?")
                    .setPositiveButton("Xác nhận", (dialog1, which) -> {
                        createOrder(selectedItems, name, address, phoneDigits);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        btnHuy.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void createOrder(List<CartItem> selectedItems, String receiverName, String receiverAddress, String receiverPhone) {
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> cartItemIds = new ArrayList<>();
        for (CartItem item : selectedItems) {
            cartItemIds.add(item.getId());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("cart_item_ids", cartItemIds);
        body.put("receiver_name", receiverName);
        body.put("receiver_address", receiverAddress);
        body.put("receiver_phone", receiverPhone);
        // Voucher sẽ được tự động áp dụng ở backend

        Log.d("CreateOrder", "Creating order with userId: " + userId);
        Log.d("CreateOrder", "Cart item IDs: " + cartItemIds);
        Log.d("CreateOrder", "Receiver: " + receiverName + ", " + receiverAddress + ", " + receiverPhone);

        apiServices.createOrder(body).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(Call<Response<Order>> call, retrofit2.Response<Response<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Order> res = response.body();
                    Log.d("CreateOrder", "Response success: " + res.isSuccess() + ", Message: " + res.getMessage());
                    if (res.isSuccess()) {
                        Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        loadCart(); // Reload cart
                        
                        // Chuyển sang tab đơn hàng (index 2)
                        if (getActivity() instanceof ManchinhUser) {
                            ((ManchinhUser) getActivity()).selectTab(2);
                        }
                    } else {
                        String errorMsg = res.getMessage() != null ? res.getMessage() : "Đặt hàng thất bại";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e("CreateOrder", "Order failed: " + errorMsg);
                    }
                } else {
                    String errorMsg = "Lỗi đặt hàng";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("CreateOrder", "Response error: " + response.code() + ", " + errorMsg);
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Order>> call, Throwable t) {
                Log.e("CreateOrder", "Failure: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId != null) {
            loadCart();
        }
    }
}
