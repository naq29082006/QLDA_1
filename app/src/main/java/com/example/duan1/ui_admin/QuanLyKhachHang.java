package com.example.duan1.ui_admin;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.AdapterAdmin.KhachHangAdapter;
import com.example.duan1.R;
import com.example.duan1.model.Response;
import com.example.duan1.model.User;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.PollingHelper;
import com.example.duan1.utils.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class QuanLyKhachHang extends AppCompatActivity {

    private RecyclerView rvKhachHang;
    private KhachHangAdapter adapter;
    private List<User> khachHangList;
    private TextInputEditText edtSearchKhachHang;
    private ImageView imgBack;
    private ApiServices apiServices;
    private PollingHelper pollingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_khach_hang);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ views
        rvKhachHang = findViewById(R.id.rvKhachHang);
        edtSearchKhachHang = findViewById(R.id.edtSearchKhachHang);
        imgBack = findViewById(R.id.imgBack);
        
        // Xử lý nút back
        imgBack.setOnClickListener(v -> finish());

        // Khởi tạo danh sách
        khachHangList = new ArrayList<>();

        // Setup RecyclerView
        adapter = new KhachHangAdapter(this, khachHangList);
        rvKhachHang.setLayoutManager(new LinearLayoutManager(this));
        rvKhachHang.setAdapter(adapter);

        // Load khách hàng từ API
        loadUsers();

        // Khởi tạo PollingHelper để tự động cập nhật mỗi 5 giây
        pollingHelper = new PollingHelper("QuanLyKhachHang", 5000);
        pollingHelper.setRefreshCallback(() -> {
            // Chỉ refresh nếu không đang search
            if (edtSearchKhachHang.getText().toString().trim().isEmpty()) {
                loadUsers();
            }
        });
        pollingHelper.startPolling();

        // Search
        edtSearchKhachHang.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý View Detail và Delete (bỏ Edit)
        adapter.setOnItemClickListener(new KhachHangAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Lấy user từ adapter (đã xử lý filter)
                List<User> currentList = adapter.getKhachHangList();
                if (position >= 0 && position < currentList.size()) {
                    User user = currentList.get(position);
                    // Tìm user trong khachHangList để lấy đúng object
                    User realUser = null;
                    for (User u : khachHangList) {
                        if (u.getId().equals(user.getId())) {
                            realUser = u;
                            break;
                        }
                    }
                    if (realUser != null) {
                        showChiTietKhachHangDialog(realUser);
                    }
                }
            }

            @Override
            public void onEditClick(int position) {
                // Không làm gì - đã bỏ chức năng edit
            }

            @Override
            public void onDeleteClick(int position) {
                // Lấy user từ adapter (đã xử lý filter)
                List<User> currentList = adapter.getKhachHangList();
                if (position >= 0 && position < currentList.size()) {
                    User user = currentList.get(position);
                    // Confirm dialog trước khi xóa
                    new android.app.AlertDialog.Builder(QuanLyKhachHang.this)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa khách hàng " + (user.getName() != null ? user.getName() : user.getEmail()) + "?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                deleteUser(user.getId());
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                }
            }
        });
    }

    private void loadUsers() {
        apiServices.getAllUsers().enqueue(new Callback<Response<List<User>>>() {
            @Override
            public void onResponse(Call<Response<List<User>>> call, retrofit2.Response<Response<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<List<User>> res = response.body();
                    Log.d("API Response", "Success: " + res.isSuccess() + ", Data: " + (res.getData() != null ? res.getData().size() : "null"));
                    if (res.isSuccess() && res.getData() != null && !res.getData().isEmpty()) {
                        khachHangList.clear();
                        khachHangList.addAll(res.getData());
                        adapter.updateList(khachHangList);
                        Log.d("API", "Loaded " + khachHangList.size() + " users");
                    } else {
                        String message = res.getMessage() != null ? res.getMessage() : "Không có khách hàng nào";
                        Toast.makeText(QuanLyKhachHang.this, message, Toast.LENGTH_SHORT).show();
                        Log.d("API", "No users: " + message);
                    }
                } else {
                    String errorMsg = "Lỗi khi tải khách hàng";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(QuanLyKhachHang.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("API Error", "Response code: " + response.code() + ", Message: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Response<List<User>>> call, Throwable t) {
                Toast.makeText(QuanLyKhachHang.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Failure: " + t.getMessage(), t);
            }
        });
    }

    private void showChiTietKhachHangDialog(User user) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chi_tiet_khach_hang);
        
        // Set width cho dialog
        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setAttributes(params);

        TextView tvTen = dialog.findViewById(R.id.tvChiTietTen);
        TextView tvEmail = dialog.findViewById(R.id.tvChiTietEmail);
        TextView tvPhone = dialog.findViewById(R.id.tvChiTietPhone);
        TextView tvRole = dialog.findViewById(R.id.tvChiTietRole);
        TextView tvId = dialog.findViewById(R.id.tvChiTietId);
        Button btnDong = dialog.findViewById(R.id.btnDong);

        tvTen.setText(user.getName() != null ? user.getName() : "Chưa có tên");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa có email");
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa có số điện thoại");
        String roleText = "user".equals(user.getRole()) ? "Khách hàng" : ("admin".equals(user.getRole()) ? "Quản trị viên" : (user.getRole() != null ? user.getRole() : "Chưa có"));
        tvRole.setText(roleText);
        tvId.setText(user.getId() != null ? user.getId() : "Chưa có ID");

        btnDong.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditKhachHangDialog(User user, int position) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sua_khach_hang);
        
        // Set width cho dialog
        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setAttributes(params);

        EditText edtName = dialog.findViewById(R.id.edtTenKH);
        EditText edtEmail = dialog.findViewById(R.id.edtEmail);
        EditText edtPhone = dialog.findViewById(R.id.edtPhone);
        Spinner spinnerRole = dialog.findViewById(R.id.spinnerRole);
        Button btnCapNhat = dialog.findViewById(R.id.btnCapNhat);
        Button btnCancel = dialog.findViewById(R.id.btnHuy);

        edtName.setText(user.getName());
        edtEmail.setText(user.getEmail());
        edtPhone.setText(user.getPhone());

        // Setup spinner role
        List<String> roles = new ArrayList<>();
        roles.add("user");
        roles.add("admin");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roles) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(android.R.color.black));
                textView.setTextSize(18);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(android.R.color.black));
                textView.setTextSize(18);
                return view;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(spinnerAdapter);
        
        // Set selection
        int roleIndex = "admin".equals(user.getRole()) ? 1 : 0;
        spinnerRole.setSelection(roleIndex);

        btnCapNhat.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String role = roles.get(spinnerRole.getSelectedItemPosition());

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            updateUser(user.getId(), name, email, phone, role, position, dialog);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateUser(String id, String name, String email, String phone, String role, int position, Dialog dialog) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("phone", phone);
        body.put("role", role);

        apiServices.updateUser(id, body).enqueue(new Callback<Response<User>>() {
            @Override
            public void onResponse(Call<Response<User>> call, retrofit2.Response<Response<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<User> res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(QuanLyKhachHang.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // Load lại danh sách từ API
                        loadUsers();
                    } else {
                        Toast.makeText(QuanLyKhachHang.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QuanLyKhachHang.this, "Lỗi khi cập nhật khách hàng", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", response.toString());
                }
            }

            @Override
            public void onFailure(Call<Response<User>> call, Throwable t) {
                Toast.makeText(QuanLyKhachHang.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", t.toString());
            }
        });
    }

    private void deleteUser(String id) {
        apiServices.deleteUser(id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Void> res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(QuanLyKhachHang.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        // Load lại danh sách từ API
                        loadUsers();
                    } else {
                        Toast.makeText(QuanLyKhachHang.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QuanLyKhachHang.this, "Lỗi khi xóa khách hàng", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", response.toString());
                }
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                Toast.makeText(QuanLyKhachHang.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", t.toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollingHelper != null) {
            pollingHelper.stopPolling();
        }
    }
}

