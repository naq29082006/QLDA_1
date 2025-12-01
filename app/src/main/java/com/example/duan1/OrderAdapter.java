package com.example.duan1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private List<Order> orderListFull;
    private Context context;
    private OnDetailClickListener listener;

    public interface OnDetailClickListener {
        void onDetailClick(Order order);
    }

    public void setOnDetailClickListener(OnDetailClickListener listener) {
        this.listener = listener;
    }

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.orderListFull = new ArrayList<>(orderList);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.tvOrderId.setText(order.getOrderId());
        holder.tvStatus.setText(order.getStatus());
        
        // Set màu cho trạng thái
        holder.tvStatus.setTextColor(order.getStatusColor());
        
        // Set màu cho icon
        holder.viewIcon.setBackgroundColor(order.getIconColor());

        holder.btnChiTiet.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void filterList(String text) {
        orderList.clear();
        if (text == null || text.trim().isEmpty()) {
            orderList.addAll(orderListFull);
        } else {
            text = text.toLowerCase().trim();
            for (Order order : orderListFull) {
                if (order.getOrderId().toLowerCase().contains(text) ||
                    order.getStatus().toLowerCase().contains(text)) {
                    orderList.add(order);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        View viewIcon;
        TextView tvOrderId, tvStatus;
        Button btnChiTiet;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIcon = itemView.findViewById(R.id.viewIcon);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnChiTiet = itemView.findViewById(R.id.btnChiTiet);
        }
    }
}

