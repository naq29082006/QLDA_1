package com.example.duan1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private OnActionClickListener actionListener;
    private String orderStatusFilter; // "đang chờ", "đang chuẩn bị", "đang giao", "đã giao"

    public interface OnDetailClickListener {
        void onDetailClick(Order order);
    }

    public interface OnActionClickListener {
        void onXacNhanClick(Order order, int position);
        void onHuyClick(Order order, int position);
        void onBatDauGiaoClick(Order order, int position);
        void onGiaoThanhCongClick(Order order, int position);
        void onGiaoThatBaiClick(Order order, int position);
        void onDanhGiaClick(Order order, int position);
    }

    public void setOnDetailClickListener(OnDetailClickListener listener) {
        this.listener = listener;
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        this.actionListener = listener;
    }

    public void setOrderStatusFilter(String status) {
        this.orderStatusFilter = status;
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
        
        // Hiển thị Order ID (nếu có orderId thì dùng, không thì dùng id)
        String orderIdText = order.getOrderId() != null ? order.getOrderId() : 
                            (order.getId() != null ? order.getId().substring(order.getId().length() - 6) : "N/A");
        holder.tvOrderId.setText(orderIdText);
        
        // Hiển thị trạng thái
        String status = order.getStatus() != null ? order.getStatus() : "Chưa xác định";
        holder.tvStatus.setText(status);
        
        // Set màu cho trạng thái
        holder.tvStatus.setTextColor(order.getStatusColor());
        
        // Set màu cho icon
        holder.viewIcon.setBackgroundColor(order.getIconColor());

        // Ẩn tất cả các nút trước
        holder.btnXacNhan.setVisibility(View.GONE);
        holder.btnHuy.setVisibility(View.GONE);
        holder.btnBatDauGiao.setVisibility(View.GONE);
        holder.btnGiaoThanhCong.setVisibility(View.GONE);
        holder.btnGiaoThatBai.setVisibility(View.GONE);
        holder.btnChiTiet.setVisibility(View.GONE);
        holder.btnDanhGia.setVisibility(View.GONE);

        // Hiển thị nút theo trạng thái
        if (orderStatusFilter != null) {
            // Admin view: không có nút chi tiết, click vào item để xem chi tiết
            holder.btnChiTiet.setVisibility(View.GONE);
            switch (orderStatusFilter.toLowerCase()) {
                case "chờ xác nhận":
                case "đang chờ":
                case "pending":
                    holder.btnXacNhan.setVisibility(View.VISIBLE);
                    holder.btnHuy.setVisibility(View.VISIBLE);
                    break;
                case "chờ lấy hàng":
                case "đang chuẩn bị":
                case "preparing":
                    holder.btnBatDauGiao.setVisibility(View.VISIBLE);
                    break;
                case "đang giao":
                case "delivering":
                    holder.btnGiaoThanhCong.setVisibility(View.VISIBLE);
                    // Bỏ nút Giao thất bại - chỉ có nút thành công
                    holder.btnGiaoThatBai.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            
            // Thêm click listener cho toàn bộ item view ở admin
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailClick(order);
                }
            });
        } else {
            // User view: luôn hiển thị nút chi tiết và đánh giá (nếu đã nhận)
            holder.btnChiTiet.setVisibility(View.VISIBLE);
            
            // Hiển thị nút đánh giá nếu đơn hàng đã nhận (không phải đã hủy)
            String statusLower = status.toLowerCase();
            if (statusLower.contains("đã nhận") || statusLower.contains("đã giao") || statusLower.contains("delivered") || statusLower.contains("giao thành công")) {
                holder.btnDanhGia.setVisibility(View.VISIBLE);
                // Kiểm tra xem có text đã lưu không
                String buttonText = reviewButtonTexts.get(position);
                holder.btnDanhGia.setText(buttonText != null ? buttonText : "Đánh giá");
            }
        }

        // Xử lý click các nút
        holder.btnXacNhan.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onXacNhanClick(order, position);
            }
        });

        holder.btnHuy.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onHuyClick(order, position);
            }
        });

        holder.btnBatDauGiao.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onBatDauGiaoClick(order, position);
            }
        });

        holder.btnGiaoThanhCong.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onGiaoThanhCongClick(order, position);
            }
        });

        holder.btnGiaoThatBai.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onGiaoThatBaiClick(order, position);
            }
        });

        holder.btnChiTiet.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailClick(order);
            }
        });

        holder.btnDanhGia.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDanhGiaClick(order, position);
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
                String orderId = order.getOrderId() != null ? order.getOrderId() : "";
                String status = order.getStatus() != null ? order.getStatus() : "";
                if (orderId.toLowerCase().contains(text) || status.toLowerCase().contains(text)) {
                    orderList.add(order);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList != null ? newList : new ArrayList<>();
        this.orderListFull = new ArrayList<>(orderList);
        reviewButtonTexts.clear(); // Clear review button texts khi update list
        notifyDataSetChanged();
    }
    
    private java.util.Map<Integer, String> reviewButtonTexts = new java.util.HashMap<>();
    
    public void updateReviewButtonText(int position, String text) {
        if (position >= 0 && position < orderList.size()) {
            reviewButtonTexts.put(position, text);
            notifyItemChanged(position);
        }
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        View viewIcon;
        TextView tvOrderId, tvStatus;
        Button btnXacNhan, btnHuy, btnBatDauGiao, btnGiaoThanhCong, btnGiaoThatBai, btnChiTiet, btnDanhGia;
        LinearLayout layoutActions;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIcon = itemView.findViewById(R.id.viewIcon);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnXacNhan = itemView.findViewById(R.id.btnXacNhan);
            btnHuy = itemView.findViewById(R.id.btnHuy);
            btnBatDauGiao = itemView.findViewById(R.id.btnBatDauGiao);
            btnGiaoThanhCong = itemView.findViewById(R.id.btnGiaoThanhCong);
            btnGiaoThatBai = itemView.findViewById(R.id.btnGiaoThatBai);
            btnChiTiet = itemView.findViewById(R.id.btnChiTiet);
            btnDanhGia = itemView.findViewById(R.id.btnDanhGia);
        }
    }
}

