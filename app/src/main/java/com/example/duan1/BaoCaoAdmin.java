package com.example.duan1;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.duan1.model.Response;
import com.example.duan1.model.TopCustomer;
import com.example.duan1.model.TopProduct;
import com.example.duan1.model.TopRevenue;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class BaoCaoAdmin extends AppCompatActivity {
    private RecyclerView rvTabs, rvData;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imgBack;
    private EditText edtLimit, edtStartDate, edtEndDate;
    private TextView tvFilter;
    
    private ApiServices apiServices;
    private BaoCaoTabAdapter tabAdapter;
    private TopRevenueAdapter revenueAdapter;
    private TopCustomerAdapter customerAdapter;
    private TopProductAdapter productAdapter;
    
    private String selectedTab = "Top Khách Hàng";
    private int limit = 5;
    private String startDate = "";
    private String endDate = "";
    
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bao_cao_admin);

        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ views
        rvTabs = findViewById(R.id.rvTabs);
        rvData = findViewById(R.id.rvData);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        imgBack = findViewById(R.id.imgBack);
        edtLimit = findViewById(R.id.edtLimit);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        tvFilter = findViewById(R.id.tvFilter);

        // Set default values
        edtLimit.setText(String.valueOf(limit));
        Calendar today = Calendar.getInstance();
        Calendar weekAgo = Calendar.getInstance();
        weekAgo.add(Calendar.DAY_OF_MONTH, -7);
        endDate = dateFormat.format(today.getTime());
        startDate = dateFormat.format(weekAgo.getTime());
        edtStartDate.setText(formatDisplayDate(startDate));
        edtEndDate.setText(formatDisplayDate(endDate));

        // Setup tabs
        tabAdapter = new BaoCaoTabAdapter(this);
        rvTabs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTabs.setAdapter(tabAdapter);
        tabAdapter.setOnTabClickListener(tab -> {
            selectedTab = tab;
            loadData();
        });

        // Setup adapters
        revenueAdapter = new TopRevenueAdapter(this);
        customerAdapter = new TopCustomerAdapter(this);
        productAdapter = new TopProductAdapter(this);

        // Setup RecyclerView
        rvData.setLayoutManager(new LinearLayoutManager(this));

        // Date pickers
        edtStartDate.setOnClickListener(v -> showDatePicker(true));
        edtEndDate.setOnClickListener(v -> showDatePicker(false));

        // Filter button
        tvFilter.setOnClickListener(v -> {
            try {
                String limitStr = edtLimit.getText().toString().trim();
                if (limitStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                    return;
                }
                limit = Integer.parseInt(limitStr);
                if (limit <= 0) {
                    Toast.makeText(this, "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadData();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button
        imgBack.setOnClickListener(v -> finish());

        // Swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> loadData());

        // Load initial data
        loadData();
    }

    private void showDatePicker(boolean isStartDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = dateFormat.format(calendar.getTime());
                    if (isStartDate) {
                        startDate = selectedDate;
                        edtStartDate.setText(formatDisplayDate(selectedDate));
                    } else {
                        endDate = selectedDate;
                        edtEndDate.setText(formatDisplayDate(selectedDate));
                    }
                    loadData();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String formatDisplayDate(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            return date;
        }
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);
        
        if (selectedTab.equals("Top Khách Hàng")) {
            loadTopCustomers();
        } else if (selectedTab.equals("Top Sản Phẩm")) {
            loadTopProducts();
        } else if (selectedTab.equals("Top Doanh Thu")) {
            loadTopRevenue();
        }
    }

    private void loadTopRevenue() {
        rvData.setAdapter(revenueAdapter);
        Log.d("BaoCaoAdmin", "Loading top revenue - limit: " + limit + ", startDate: " + startDate + ", endDate: " + endDate);
        apiServices.getTopRevenue(limit, startDate, endDate).enqueue(new Callback<Response<List<TopRevenue>>>() {
            @Override
            public void onResponse(Call<Response<List<TopRevenue>>> call, retrofit2.Response<Response<List<TopRevenue>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<TopRevenue>> res = response.body();
                    Log.d("BaoCaoAdmin", "Top revenue response - success: " + res.isSuccess() + ", status: " + res.getStatus());
                    if (res.isSuccess() && res.getData() != null) {
                        Log.d("BaoCaoAdmin", "Top revenue data count: " + res.getData().size());
                        revenueAdapter.updateList(res.getData());
                        updateEmptyState(res.getData().isEmpty());
                    } else {
                        Log.w("BaoCaoAdmin", "Top revenue - no data or not success. Message: " + res.getMessage());
                        revenueAdapter.updateList(new ArrayList<>());
                        updateEmptyState(true);
                        if (res.getMessage() != null) {
                            Toast.makeText(BaoCaoAdmin.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e("BaoCaoAdmin", "Top revenue - response not successful. Code: " + response.code());
                    revenueAdapter.updateList(new ArrayList<>());
                    updateEmptyState(true);
                    String errorMsg = "Lỗi khi tải dữ liệu";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                            Log.e("BaoCaoAdmin", "Error body: " + errorMsg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(BaoCaoAdmin.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<List<TopRevenue>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                revenueAdapter.updateList(new ArrayList<>());
                updateEmptyState(true);
                Log.e("BaoCaoAdmin", "Load top revenue failure: " + t.getMessage(), t);
                Toast.makeText(BaoCaoAdmin.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTopCustomers() {
        rvData.setAdapter(customerAdapter);
        Log.d("BaoCaoAdmin", "Loading top customers - limit: " + limit + ", startDate: " + startDate + ", endDate: " + endDate);
        apiServices.getTopCustomers(limit, startDate, endDate).enqueue(new Callback<Response<List<TopCustomer>>>() {
            @Override
            public void onResponse(Call<Response<List<TopCustomer>>> call, retrofit2.Response<Response<List<TopCustomer>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<TopCustomer>> res = response.body();
                    Log.d("BaoCaoAdmin", "Top customers response - success: " + res.isSuccess() + ", status: " + res.getStatus());
                    if (res.isSuccess() && res.getData() != null) {
                        Log.d("BaoCaoAdmin", "Top customers data count: " + res.getData().size());
                        customerAdapter.updateList(res.getData());
                        updateEmptyState(res.getData().isEmpty());
                    } else {
                        Log.w("BaoCaoAdmin", "Top customers - no data or not success. Message: " + res.getMessage());
                        customerAdapter.updateList(new ArrayList<>());
                        updateEmptyState(true);
                        if (res.getMessage() != null) {
                            Toast.makeText(BaoCaoAdmin.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e("BaoCaoAdmin", "Top customers - response not successful. Code: " + response.code());
                    customerAdapter.updateList(new ArrayList<>());
                    updateEmptyState(true);
                    String errorMsg = "Lỗi khi tải dữ liệu";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                            Log.e("BaoCaoAdmin", "Error body: " + errorMsg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(BaoCaoAdmin.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<List<TopCustomer>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                customerAdapter.updateList(new ArrayList<>());
                updateEmptyState(true);
                Log.e("BaoCaoAdmin", "Load top customers failure: " + t.getMessage(), t);
                Toast.makeText(BaoCaoAdmin.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTopProducts() {
        rvData.setAdapter(productAdapter);
        Log.d("BaoCaoAdmin", "Loading top products - limit: " + limit + ", startDate: " + startDate + ", endDate: " + endDate);
        apiServices.getTopProducts(limit, startDate, endDate).enqueue(new Callback<Response<List<TopProduct>>>() {
            @Override
            public void onResponse(Call<Response<List<TopProduct>>> call, retrofit2.Response<Response<List<TopProduct>>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<TopProduct>> res = response.body();
                    Log.d("BaoCaoAdmin", "Top products response - success: " + res.isSuccess() + ", status: " + res.getStatus());
                    if (res.isSuccess() && res.getData() != null) {
                        Log.d("BaoCaoAdmin", "Top products data count: " + res.getData().size());
                        productAdapter.updateList(res.getData());
                        updateEmptyState(res.getData().isEmpty());
                    } else {
                        Log.w("BaoCaoAdmin", "Top products - no data or not success. Message: " + res.getMessage());
                        productAdapter.updateList(new ArrayList<>());
                        updateEmptyState(true);
                        if (res.getMessage() != null) {
                            Toast.makeText(BaoCaoAdmin.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e("BaoCaoAdmin", "Top products - response not successful. Code: " + response.code());
                    productAdapter.updateList(new ArrayList<>());
                    updateEmptyState(true);
                    String errorMsg = "Lỗi khi tải dữ liệu";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                            Log.e("BaoCaoAdmin", "Error body: " + errorMsg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(BaoCaoAdmin.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<List<TopProduct>>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                productAdapter.updateList(new ArrayList<>());
                updateEmptyState(true);
                Log.e("BaoCaoAdmin", "Load top products failure: " + t.getMessage(), t);
                Toast.makeText(BaoCaoAdmin.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvData.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvData.setVisibility(View.VISIBLE);
        }
    }
}

