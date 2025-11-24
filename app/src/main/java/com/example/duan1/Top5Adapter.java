package com.example.duan1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Top5Adapter extends RecyclerView.Adapter<Top5Adapter.ViewHolder> {

    private List<Top5Response.TopOrder> list;

    public Top5Adapter(List<Top5Response.TopOrder> list) {
        this.list = list;
    }

    public void updateData(List<Top5Response.TopOrder> newList) {
        if(newList != null) {
            this.list = newList;
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProduct, tvCustomer, tvDays;
        public ViewHolder(View item) {
            super(item);
            tvProduct = item.findViewById(R.id.tvProduct);
            tvCustomer = item.findViewById(R.id.tvCustomer);
            tvDays = item.findViewById(R.id.tvDays);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top5, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Top5Response.TopOrder order = list.get(position);
        holder.tvProduct.setText(order.product);
        holder.tvCustomer.setText(order.customer);
        holder.tvDays.setText("Chờ: " + order.daysWaiting + " ngày");
        // Highlight nếu chờ > 5 ngày
        if(order.daysWaiting > 5){
            holder.tvDays.setTextColor(0xFFFF5722); // màu cam
        } else {
            holder.tvDays.setTextColor(0xFFFF0000); // màu đỏ
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

}