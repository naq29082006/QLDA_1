package com.example.duan1.ui_admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.duan1.QuanLyVoucher;
import com.example.duan1.R;
import com.example.duan1.ThongKeBaoHanh;
import com.example.duan1.QuanLyDanhGia;
import com.example.duan1.QuanLyDonHangAdmin;
import com.example.duan1.BaoCaoAdmin;

public class TrangChuFragment extends Fragment {

    private CardView cardQuanLyDanhMuc, cardQuanLySanPham, cardQuanLyThongKe;
    private CardView cardQuanLyKhachHang, cardQuanLyDanhGia, cardQuanLyDonHang, cardQuanLyVoucher;

    public TrangChuFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trang_chu_fragment, container, false);

        // Ánh xạ các card
        cardQuanLyDanhMuc = view.findViewById(R.id.cardQuanLyDanhMuc);
        cardQuanLySanPham = view.findViewById(R.id.cardQuanLySanPham);
        cardQuanLyThongKe = view.findViewById(R.id.cardQuanLyThongKe);
        cardQuanLyKhachHang = view.findViewById(R.id.cardQuanLyKhachHang);
        cardQuanLyDanhGia = view.findViewById(R.id.cardQuanLyDanhGia);
        cardQuanLyDonHang = view.findViewById(R.id.cardQuanLyDonHang);
        cardQuanLyVoucher = view.findViewById(R.id.cardQuanLyVoucher);

        // Xử lý click cho từng card
        cardQuanLyDanhMuc.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuanLyDanhMuc.class);
            startActivity(intent);
        });

        cardQuanLySanPham.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuanLySanPham.class);
            startActivity(intent);
        });

        cardQuanLyThongKe.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BaoCaoAdmin.class);
            startActivity(intent);
        });

        cardQuanLyKhachHang.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuanLyKhachHang.class);
            startActivity(intent);
        });

        cardQuanLyDanhGia.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuanLyDanhGia.class);
            startActivity(intent);
        });

        // Xử lý click vào card quản lý đơn hàng
        cardQuanLyDonHang.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuanLyDonHangAdmin.class);
            startActivity(intent);
        });

        cardQuanLyVoucher.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuanLyVoucher.class);
            startActivity(intent);
        });

        return view;
    }
}
