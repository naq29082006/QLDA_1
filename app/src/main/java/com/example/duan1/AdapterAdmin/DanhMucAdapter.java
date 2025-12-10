package com.example.duan1.AdapterAdmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.ui_admin.QuanLyDanhMuc;
import com.example.duan1.R;
import com.example.duan1.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;

public class DanhMucAdapter extends RecyclerView.Adapter<DanhMucAdapter.DanhMucViewHolder> {

    private List<DanhMuc> danhMucList;
    private List<DanhMuc> danhMucListFiltered;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public DanhMucAdapter(QuanLyDanhMuc activity, List<DanhMuc> danhMucList) {
        this.danhMucList = danhMucList;
        this.danhMucListFiltered = new ArrayList<>(danhMucList);
    }

    public void updateList(List<DanhMuc> newList) {
        this.danhMucList = newList;
        this.danhMucListFiltered = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DanhMucViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_danh_muc, parent, false);
        return new DanhMucViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DanhMucViewHolder holder, int position) {
        DanhMuc danhMuc = danhMucListFiltered.get(position);
        holder.tvRank.setText("#" + (position + 1));
        holder.tvTenDanhMuc.setText(danhMuc.getName());
        holder.tvMoTa.setText(danhMuc.getDescription());

        // Click vào item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int pos = danhMucList.indexOf(danhMuc);
                if (pos != -1) listener.onItemClick(pos);
            }
        });

        holder.imgEdit.setOnClickListener(v -> {
            if (listener != null) {
                int pos = danhMucList.indexOf(danhMuc);
                if (pos != -1) listener.onEditClick(pos);
            }
        });

        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                int pos = danhMucList.indexOf(danhMuc);
                if (pos != -1) listener.onDeleteClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhMucListFiltered.size();
    }

    public void filterList(String searchText) {
        danhMucListFiltered.clear();
        if (searchText.isEmpty()) {
            danhMucListFiltered.addAll(danhMucList);
        } else {
            for (DanhMuc danhMuc : danhMucList) {
                if (danhMuc.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    danhMucListFiltered.add(danhMuc);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class DanhMucViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTenDanhMuc, tvMoTa;
        ImageView imgEdit, imgDelete;

        public DanhMucViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvTenDanhMuc = itemView.findViewById(R.id.tvTenDanhMuc);
            tvMoTa = itemView.findViewById(R.id.tvMoTa);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}

