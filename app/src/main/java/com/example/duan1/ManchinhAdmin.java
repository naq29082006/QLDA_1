package com.example.duan1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ManchinhAdmin extends AppCompatActivity {
    TextView tvTenKhachHang, tvSuaThongTin;
    Button btnXemSanPham, btnGioHang, btnLichSuDonHang, btnDatHang, btnTaiKhoan, btnLogin;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manchinh_admin);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ các view
        tvTenKhachHang = findViewById(R.id.tvTenKhachHang);
        tvSuaThongTin = findViewById(R.id.tvSuaThongTin);
        btnXemSanPham = findViewById(R.id.btnXemSanPham);
        btnGioHang = findViewById(R.id.btnGioHang);
        btnLichSuDonHang = findViewById(R.id.btnLichSuDonHang);
        btnDatHang = findViewById(R.id.btnDatHang);
        btnTaiKhoan = findViewById(R.id.btnTaiKhoan);
        btnLogin = findViewById(R.id.btnLogin);

        // Lấy thông tin người dùng từ SharedPreferences
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String tenKhachHang = sharedPreferences.getString("name", "Khách hàng");
        tvTenKhachHang.setText("Xin chào, " + tenKhachHang);

        // Xử lý sự kiện click các nút
        btnXemSanPham.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManchinhAdmin.this, XemSanPham.class);
                startActivity(intent);
            }
        });

        btnGioHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManchinhAdmin.this, "Giỏ hàng", Toast.LENGTH_SHORT).show();
                // TODO: Chuyển đến màn hình giỏ hàng
            }
        });

        btnLichSuDonHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManchinhAdmin.this, "Lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                // TODO: Chuyển đến màn hình lịch sử đơn hàng
            }
        });

        btnDatHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManchinhAdmin.this, "Đặt hàng", Toast.LENGTH_SHORT).show();
                // TODO: Chuyển đến màn hình đặt hàng
            }
        });

        btnTaiKhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManchinhAdmin.this, "Tài khoản", Toast.LENGTH_SHORT).show();
                // TODO: Chuyển đến màn hình tài khoản
            }
        });

        tvSuaThongTin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManchinhAdmin.this, "Sửa thông tin tài khoản", Toast.LENGTH_SHORT).show();
                // TODO: Chuyển đến màn hình sửa thông tin
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển đến màn hình đăng nhập
                Intent intent = new Intent(ManchinhAdmin.this, Dangnhap.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
