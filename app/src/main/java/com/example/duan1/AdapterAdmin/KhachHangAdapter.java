package com.example.duan1.AdapterAdmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.R;
import com.example.duan1.model.User;

import java.util.ArrayList;
import java.util.List;

public class KhachHangAdapter extends RecyclerView.Adapter<KhachHangAdapter.KhachHangViewHolder> {

    private List<User> khachHangList;
    private List<User> khachHangListFiltered;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public KhachHangAdapter(com.example.duan1.ui_admin.QuanLyKhachHang activity, List<User> khachHangList) {
        this.khachHangList = khachHangList;
        this.khachHangListFiltered = new ArrayList<>(khachHangList);
    }

    public void updateList(List<User> newList) {
        this.khachHangList = newList;
        this.khachHangListFiltered = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public List<User> getKhachHangList() {
        return khachHangListFiltered;
    }

    @NonNull
    @Override
    public KhachHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_khach_hang, parent, false);
        return new KhachHangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KhachHangViewHolder holder, int position) {
        User user = khachHangListFiltered.get(position);
        holder.tvRank.setText("#" + (position + 1));
        holder.tvTenKhachHang.setText(user.getName() != null ? user.getName() : "Chưa có tên");
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "Chưa có email");
        holder.tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa có số điện thoại");

        // Click vào item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int pos = khachHangList.indexOf(user);
                if (pos != -1) listener.onItemClick(pos);
            }
        });

        // Ẩn nút edit - chỉ cho phép xem thông tin
        holder.imgEdit.setVisibility(View.GONE);

        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                int pos = khachHangList.indexOf(user);
                if (pos != -1) listener.onDeleteClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return khachHangListFiltered.size();
    }

    public void filterList(String searchText) {
        khachHangListFiltered.clear();
        if (searchText.isEmpty()) {
            khachHangListFiltered.addAll(khachHangList);
        } else {
            for (User user : khachHangList) {
                String searchLower = searchText.toLowerCase();
                String name = user.getName() != null ? user.getName().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                String phone = user.getPhone() != null ? user.getPhone().toLowerCase() : "";
                
                if (name.contains(searchLower) || email.contains(searchLower) || phone.contains(searchLower)) {
                    khachHangListFiltered.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class KhachHangViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTenKhachHang, tvEmail, tvPhone;
        ImageView imgEdit, imgDelete;

        public KhachHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvTenKhachHang = itemView.findViewById(R.id.tvTenKhachHang);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}

