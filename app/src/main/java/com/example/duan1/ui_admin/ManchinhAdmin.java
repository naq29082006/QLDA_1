package com.example.duan1.ui_admin;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.duan1.R;

public class ManchinhAdmin extends AppCompatActivity {

    private LinearLayout navTrangChu, navTaiKhoan;
    private TextView tvTenKhachHang, tvSuaThongTin;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-Edge
        setContentView(R.layout.activity_manchinh_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ Bottom Navigation
        navTrangChu = findViewById(R.id.navTrangChu);
        navTaiKhoan = findViewById(R.id.navTaiKhoan);

        // Ánh xạ TextView nếu cần hiển thị tên khách
//        tvTenKhachHang = findViewById(R.id.tvTenKhachHang); // tạo trong XML nếu chưa có
//        tvSuaThongTin = findViewById(R.id.tvSuaThongTin);   // tạo trong XML nếu chưa có

        // SharedPreferences
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String tenKhachHang = sharedPreferences.getString("name", "Khách hàng");
        if (tvTenKhachHang != null) {
            tvTenKhachHang.setText("Xin chào, " + tenKhachHang);
        }

        // Setup click cho từng tab
        navTrangChu.setOnClickListener(v -> selectTab(0));

        navTaiKhoan.setOnClickListener(v -> selectTab(1));

        // Mặc định mở tab Trang Chủ
        selectTab(0);
    }

    private void selectTab(int index) {
        // Reset màu background tất cả tab
        navTrangChu.setBackgroundColor(Color.WHITE);
        navTaiKhoan.setBackgroundColor(Color.WHITE);

        // Đổi màu tab đang chọn
        switch (index) {
            case 0:
                navTrangChu.setBackgroundColor(Color.parseColor("#FFE0E0"));
                loadFragment(new TrangChuFragment());
                break;
            case 1:
                navTaiKhoan.setBackgroundColor(Color.parseColor("#FFE0E0"));
                loadFragment(new TaiKhoanFragment());
                break;
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
}
