package com.example.duan1;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.duan1.AdapterAdmin.VoucherAdapter;
import com.example.duan1.model.Response;
import com.example.duan1.model.Voucher;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.PollingHelper;
import com.example.duan1.utils.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;

public class QuanLyVoucher extends AppCompatActivity {

    private RecyclerView rvVoucher, rvStatusButtons;
    private VoucherAdapter adapter;
    private OrderStatusAdapter statusAdapter;
    private List<Voucher> voucherList;
    private List<Voucher> voucherListFull;
    private TextInputEditText edtSearchVoucher;
    private ImageView imgBack;
    private TextView tvEmpty;
    private FloatingActionButton fabAddVoucher;
    private ApiServices apiServices;
    private String selectedStatus = "active"; // "active", "inactive"
    
    // Polling
    private PollingHelper pollingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_voucher);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ views
        rvVoucher = findViewById(R.id.rvVoucher);
        rvStatusButtons = findViewById(R.id.rvStatusButtons);
        edtSearchVoucher = findViewById(R.id.edtSearchVoucher);
        imgBack = findViewById(R.id.imgBack);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAddVoucher = findViewById(R.id.fabAddVoucher);

        // Xử lý nút back
        imgBack.setOnClickListener(v -> finish());

        // Khởi tạo danh sách
        voucherList = new ArrayList<>();
        voucherListFull = new ArrayList<>();

        // Setup Status Buttons - chỉ có 2 tab
        List<String> statusList = new ArrayList<>();
        statusList.add("Đang hoạt động");
        statusList.add("Không hoạt động");
        statusAdapter = new OrderStatusAdapter(statusList);
        rvStatusButtons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvStatusButtons.setAdapter(statusAdapter);
        statusAdapter.setOnStatusClickListener(status -> {
            selectedStatus = status.equals("Đang hoạt động") ? "active" : "inactive";
            filterVouchers();
        });
        // Set tab đầu tiên được chọn
        statusAdapter.setSelectedPosition(0);

        // Setup RecyclerView
        adapter = new VoucherAdapter(new ArrayList<>()); // Khởi tạo với list rỗng
        rvVoucher.setLayoutManager(new LinearLayoutManager(this));
        rvVoucher.setAdapter(adapter);
        Log.d("QuanLyVoucher", "RecyclerView setup completed");

        // Xử lý click
        adapter.setOnVoucherClickListener(new VoucherAdapter.OnVoucherClickListener() {
            @Override
            public void onEditClick(Voucher voucher, int position) {
                showEditVoucherDialog(voucher, position);
            }

            @Override
            public void onDeleteClick(Voucher voucher, int position) {
                showDeleteConfirmDialog(voucher, position);
            }
        });

        // Search
        edtSearchVoucher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add button
        fabAddVoucher.setOnClickListener(v -> showAddVoucherDialog());

        // Load vouchers
        loadVouchers();

        // Polling
        pollingHelper = new PollingHelper("QuanLyVoucher", 5000);
        pollingHelper.setRefreshCallback(() -> {
            loadVouchers();
        });
        pollingHelper.startPolling();
    }

    private void loadVouchers() {
        apiServices.getAllVouchers().enqueue(new Callback<Response<List<Voucher>>>() {
            @Override
            public void onResponse(Call<Response<List<Voucher>>> call, retrofit2.Response<Response<List<Voucher>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<Voucher>> res = response.body();
                    if (res.isSuccess() && res.getData() != null) {
                        voucherListFull.clear();
                        voucherListFull.addAll(res.getData());
                        Log.d("QuanLyVoucher", "Loaded " + voucherListFull.size() + " vouchers from API");
                        Log.d("QuanLyVoucher", "Selected status: " + selectedStatus);
                        for (Voucher v : voucherListFull) {
                            Log.d("QuanLyVoucher", "Voucher: " + v.getVoucherCode() + ", isActive: " + v.isActive());
                        }
                        filterVouchers();
                        updateEmptyState();
                    } else {
                        Log.e("QuanLyVoucher", "Response not success: " + res.getMessage());
                        voucherListFull.clear();
                        filterVouchers();
                        updateEmptyState();
                    }
                } else {
                    Log.e("QuanLyVoucher", "Load vouchers failed: " + response.code() + ", " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Response<List<Voucher>>> call, Throwable t) {
                Log.e("QuanLyVoucher", "Load vouchers failure: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void filterVouchers() {
        voucherList.clear();
        Log.d("QuanLyVoucher", "Filtering vouchers. Full list size: " + voucherListFull.size() + ", Selected status: " + selectedStatus);
        
        java.util.Date now = new java.util.Date();
        
        for (Voucher voucher : voucherListFull) {
            boolean isCurrentlyActive = false;
            
            // Kiểm tra thời gian thực tế (không chỉ dựa vào is_active flag)
            if (voucher.isActive() && voucher.getStartDate() != null && voucher.getEndDate() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    
                    String startDateStr = voucher.getStartDate();
                    String endDateStr = voucher.getEndDate();
                    
                    // Parse dates
                    java.util.Date startDate = sdf.parse(startDateStr);
                    java.util.Date endDate = sdf.parse(endDateStr);
                    
                    if (startDate != null && endDate != null) {
                        // Set end date to end of day
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(endDate);
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                        cal.set(java.util.Calendar.MINUTE, 59);
                        cal.set(java.util.Calendar.SECOND, 59);
                        endDate = cal.getTime();
                        
                        isCurrentlyActive = now.compareTo(startDate) >= 0 && now.compareTo(endDate) <= 0;
                    }
                } catch (Exception e) {
                    Log.e("QuanLyVoucher", "Error parsing dates: " + e.getMessage());
                    // Fallback: chỉ check is_active flag
                    isCurrentlyActive = voucher.isActive();
                }
            }
            
            if (selectedStatus.equals("active") && isCurrentlyActive) {
                voucherList.add(voucher);
                Log.d("QuanLyVoucher", "Added active voucher: " + voucher.getVoucherCode());
            } else if (selectedStatus.equals("inactive") && !isCurrentlyActive) {
                voucherList.add(voucher);
                Log.d("QuanLyVoucher", "Added inactive voucher: " + voucher.getVoucherCode());
            }
        }
        Log.d("QuanLyVoucher", "Filtered list size: " + voucherList.size());
        
        // Update adapter trên main thread
        runOnUiThread(() -> {
            adapter.updateList(new ArrayList<>(voucherList));
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        if (voucherList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvVoucher.setVisibility(View.GONE);
            Log.d("QuanLyVoucher", "Empty state shown");
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvVoucher.setVisibility(View.VISIBLE);
            Log.d("QuanLyVoucher", "RecyclerView shown with " + voucherList.size() + " items");
        }
    }

    private void showAddVoucherDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_them_voucher);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputEditText edtVoucherCode = dialog.findViewById(R.id.edtVoucherCode);
        TextInputEditText edtTitle = dialog.findViewById(R.id.edtTitle);
        TextInputEditText edtDiscountPercentage = dialog.findViewById(R.id.edtDiscountPercentage);
        TextInputEditText edtMaxDiscountAmount = dialog.findViewById(R.id.edtMaxDiscountAmount);
        TextInputEditText edtStartDate = dialog.findViewById(R.id.edtStartDate);
        TextInputEditText edtEndDate = dialog.findViewById(R.id.edtEndDate);
        SwitchCompat switchIsActive = dialog.findViewById(R.id.switchIsActive);
        Button btnThem = dialog.findViewById(R.id.btnThem);
        Button btnHuy = dialog.findViewById(R.id.btnHuy);
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);

        tvDialogTitle.setText("Thêm Voucher");
        btnThem.setText("Thêm");

        // Set default dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        edtStartDate.setText(sdf.format(cal.getTime()));
        cal.add(Calendar.DAY_OF_MONTH, 30);
        edtEndDate.setText(sdf.format(cal.getTime()));

        // Date Picker cho Start Date
        edtStartDate.setFocusable(false);
        edtStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                if (!edtStartDate.getText().toString().isEmpty()) {
                    calendar.setTime(sdf.parse(edtStartDate.getText().toString()));
                }
            } catch (Exception e) {
                // Ignore
            }
            
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    edtStartDate.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Date Picker cho End Date
        edtEndDate.setFocusable(false);
        edtEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                if (!edtEndDate.getText().toString().isEmpty()) {
                    calendar.setTime(sdf.parse(edtEndDate.getText().toString()));
                }
            } catch (Exception e) {
                // Ignore
            }
            
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    edtEndDate.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnThem.setOnClickListener(v -> {
            String code = edtVoucherCode.getText().toString().trim();
            String title = edtTitle.getText().toString().trim();
            String discountStr = edtDiscountPercentage.getText().toString().trim();
            String maxDiscountStr = edtMaxDiscountAmount.getText().toString().trim();
            String startDate = edtStartDate.getText().toString().trim();
            String endDate = edtEndDate.getText().toString().trim();
            boolean isActive = switchIsActive.isChecked();

            if (code.isEmpty() || title.isEmpty() || discountStr.isEmpty() || maxDiscountStr.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double discount = Double.parseDouble(discountStr);
                if (discount < 0 || discount > 100) {
                    Toast.makeText(this, "% giảm giá phải từ 0-100", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double maxDiscount = Double.parseDouble(maxDiscountStr);
                if (maxDiscount < 0) {
                    Toast.makeText(this, "Số tiền giảm tối đa phải >= 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> body = new HashMap<>();
                body.put("voucher_code", code);
                body.put("title", title);
                body.put("discount_percentage", discount);
                body.put("max_discount_amount", maxDiscount);
                body.put("start_date", startDate);
                body.put("end_date", endDate);
                body.put("is_active", isActive);

                apiServices.createVoucher(body).enqueue(new Callback<Response<Voucher>>() {
                    @Override
                    public void onResponse(Call<Response<Voucher>> call, retrofit2.Response<Response<Voucher>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Response<Voucher> res = response.body();
                            if (res.isSuccess()) {
                                Toast.makeText(QuanLyVoucher.this, "Tạo voucher thành công", Toast.LENGTH_SHORT).show();
                                loadVouchers();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(QuanLyVoucher.this, res.getMessage() != null ? res.getMessage() : "Tạo voucher thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<Voucher>> call, Throwable t) {
                        Toast.makeText(QuanLyVoucher.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditVoucherDialog(Voucher voucher, int position) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_them_voucher);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputEditText edtVoucherCode = dialog.findViewById(R.id.edtVoucherCode);
        TextInputEditText edtTitle = dialog.findViewById(R.id.edtTitle);
        TextInputEditText edtDiscountPercentage = dialog.findViewById(R.id.edtDiscountPercentage);
        TextInputEditText edtMaxDiscountAmount = dialog.findViewById(R.id.edtMaxDiscountAmount);
        TextInputEditText edtStartDate = dialog.findViewById(R.id.edtStartDate);
        TextInputEditText edtEndDate = dialog.findViewById(R.id.edtEndDate);
        SwitchCompat switchIsActive = dialog.findViewById(R.id.switchIsActive);
        Button btnThem = dialog.findViewById(R.id.btnThem);
        Button btnHuy = dialog.findViewById(R.id.btnHuy);
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);

        tvDialogTitle.setText("Sửa Voucher");
        btnThem.setText("Cập nhật");

        // Fill data
        edtVoucherCode.setText(voucher.getVoucherCode());
        edtTitle.setText(voucher.getTitle());
        edtDiscountPercentage.setText(String.valueOf((int)voucher.getDiscountPercentage()));
        edtMaxDiscountAmount.setText(String.valueOf((long)voucher.getMaxDiscountAmount()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            if (voucher.getStartDate() != null && !voucher.getStartDate().isEmpty()) {
                // Try to parse ISO date format from MongoDB (e.g., "2024-12-01T00:00:00.000Z")
                String startDateStr = voucher.getStartDate();
                if (startDateStr.contains("T")) {
                    startDateStr = startDateStr.substring(0, startDateStr.indexOf("T"));
                }
                edtStartDate.setText(startDateStr);
            }
            if (voucher.getEndDate() != null && !voucher.getEndDate().isEmpty()) {
                String endDateStr = voucher.getEndDate();
                if (endDateStr.contains("T")) {
                    endDateStr = endDateStr.substring(0, endDateStr.indexOf("T"));
                }
                edtEndDate.setText(endDateStr);
            }
        } catch (Exception e) {
            if (voucher.getStartDate() != null) {
                edtStartDate.setText(voucher.getStartDate());
            }
            if (voucher.getEndDate() != null) {
                edtEndDate.setText(voucher.getEndDate());
            }
        }
        
        switchIsActive.setChecked(voucher.isActive());

        // Date Picker cho Start Date
        edtStartDate.setFocusable(false);
        edtStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                if (!edtStartDate.getText().toString().isEmpty()) {
                    calendar.setTime(sdf.parse(edtStartDate.getText().toString()));
                }
            } catch (Exception e) {
                // Ignore
            }
            
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    edtStartDate.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Date Picker cho End Date
        edtEndDate.setFocusable(false);
        edtEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                if (!edtEndDate.getText().toString().isEmpty()) {
                    calendar.setTime(sdf.parse(edtEndDate.getText().toString()));
                }
            } catch (Exception e) {
                // Ignore
            }
            
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    edtEndDate.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        btnThem.setOnClickListener(v -> {
            String code = edtVoucherCode.getText().toString().trim();
            String title = edtTitle.getText().toString().trim();
            String discountStr = edtDiscountPercentage.getText().toString().trim();
            String maxDiscountStr = edtMaxDiscountAmount.getText().toString().trim();
            String startDate = edtStartDate.getText().toString().trim();
            String endDate = edtEndDate.getText().toString().trim();
            boolean isActive = switchIsActive.isChecked();

            if (code.isEmpty() || title.isEmpty() || discountStr.isEmpty() || maxDiscountStr.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double discount = Double.parseDouble(discountStr);
                if (discount < 0 || discount > 100) {
                    Toast.makeText(this, "% giảm giá phải từ 0-100", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                double maxDiscount = Double.parseDouble(maxDiscountStr);
                if (maxDiscount < 0) {
                    Toast.makeText(this, "Số tiền giảm tối đa phải >= 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> body = new HashMap<>();
                body.put("voucher_code", code);
                body.put("title", title);
                body.put("discount_percentage", discount);
                body.put("max_discount_amount", maxDiscount);
                body.put("start_date", startDate);
                body.put("end_date", endDate);
                body.put("is_active", isActive);

                apiServices.updateVoucher(voucher.getId(), body).enqueue(new Callback<Response<Voucher>>() {
                    @Override
                    public void onResponse(Call<Response<Voucher>> call, retrofit2.Response<Response<Voucher>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Response<Voucher> res = response.body();
                            if (res.isSuccess()) {
                                Toast.makeText(QuanLyVoucher.this, "Cập nhật voucher thành công", Toast.LENGTH_SHORT).show();
                                loadVouchers();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(QuanLyVoucher.this, res.getMessage() != null ? res.getMessage() : "Cập nhật voucher thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Response<Voucher>> call, Throwable t) {
                        Toast.makeText(QuanLyVoucher.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        btnHuy.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteConfirmDialog(Voucher voucher, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa voucher " + voucher.getVoucherCode() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    apiServices.deleteVoucher(voucher.getId()).enqueue(new Callback<Response<Void>>() {
                        @Override
                        public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Response<Void> res = response.body();
                                if (res.isSuccess()) {
                                    Toast.makeText(QuanLyVoucher.this, "Xóa voucher thành công", Toast.LENGTH_SHORT).show();
                                    loadVouchers();
                                } else {
                                    Toast.makeText(QuanLyVoucher.this, res.getMessage() != null ? res.getMessage() : "Xóa voucher thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Response<Void>> call, Throwable t) {
                            Toast.makeText(QuanLyVoucher.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollingHelper != null) {
            pollingHelper.stopPolling();
        }
    }
}

