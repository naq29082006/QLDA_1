package com.example.duan1.ui_admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.duan1.Dangnhap;
import com.example.duan1.R;

public class TaiKhoanFragment extends Fragment {

    private TextView tvTenKhachHang, tvEmail, tvPhone;
    private Button btnDangXuat;
    private SharedPreferences sharedPreferences;

    public TaiKhoanFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tai_khoan, container, false);

        // Ánh xạ views
        tvTenKhachHang = view.findViewById(R.id.tvTenKhachHang);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        btnDangXuat = view.findViewById(R.id.btnDangXuat);

        // Lấy SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("UserData", 0);

        // Load thông tin người dùng
        loadUserInfo();

        // Xử lý đăng xuất
        btnDangXuat.setOnClickListener(v -> {
            // Xóa SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), Dangnhap.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }

    private void loadUserInfo() {
        String name = sharedPreferences.getString("name", "Chưa có thông tin");
        String email = sharedPreferences.getString("email", "Chưa có thông tin");
        String phone = sharedPreferences.getString("phone", "Chưa có thông tin");

        tvTenKhachHang.setText("Tên: " + name);
        tvEmail.setText("Email: " + email);
        tvPhone.setText("Số điện thoại: " + phone);
    }
}

