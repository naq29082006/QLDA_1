package com.example.duan1.ui_user;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.duan1.R;

public class ManchinhUser extends AppCompatActivity {

    private LinearLayout navTrangChu, navBoSuuTap, navDonHang, navTaiKhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manchinh_user);

        // Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ Bottom Navigation
        navTrangChu = findViewById(R.id.navTrangChu);
        navBoSuuTap = findViewById(R.id.navBoSuuTap);
        navDonHang = findViewById(R.id.navDonHang);
        navTaiKhoan = findViewById(R.id.navTaiKhoan);

        // Setup click cho từng tab
        navTrangChu.setOnClickListener(v -> selectTabInternal(0));
        navBoSuuTap.setOnClickListener(v -> selectTabInternal(1));
        navDonHang.setOnClickListener(v -> selectTabInternal(2));
        navTaiKhoan.setOnClickListener(v -> selectTabInternal(3));

        // Mặc định mở tab Trang Chủ
        selectTabInternal(0);
    }

    // Public method để các fragment khác có thể gọi để chuyển tab
    public void selectTab(int index) {
        selectTabInternal(index);
    }
    
    private void selectTabInternal(int index) {
        // Reset màu background tất cả tab
        navTrangChu.setBackgroundColor(Color.WHITE);
        navBoSuuTap.setBackgroundColor(Color.WHITE);
        navDonHang.setBackgroundColor(Color.WHITE);
        navTaiKhoan.setBackgroundColor(Color.WHITE);

        // Đổi màu tab đang chọn
        switch (index) {
            case 0:
                navTrangChu.setBackgroundColor(Color.parseColor("#FFE0E0"));
                loadFragment(new TrangChuUserFragment());
                break;
            case 1:
                navBoSuuTap.setBackgroundColor(Color.parseColor("#FFE0E0"));
                loadFragment(new GioHangFragment());
                break;
            case 2:
                navDonHang.setBackgroundColor(Color.parseColor("#FFE0E0"));
                loadFragment(new DonHangUserFragment());
                break;
            case 3:
                navTaiKhoan.setBackgroundColor(Color.parseColor("#FFE0E0"));
                loadFragment(new TaiKhoanUserFragment());
                break;
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
}
