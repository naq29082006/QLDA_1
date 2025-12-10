package com.example.duan1;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderStatusAdapter extends RecyclerView.Adapter<OrderStatusAdapter.StatusViewHolder> {
    private List<String> statusList;
    private Context context;
    private OnStatusClickListener listener;
    private int selectedPosition = 0;

    public interface OnStatusClickListener {
        void onStatusClick(String status);
    }

    public void setOnStatusClickListener(OnStatusClickListener listener) {
        this.listener = listener;
    }

    public OrderStatusAdapter(Context context) {
        this.context = context;
        this.statusList = new ArrayList<>();
        // Thêm 4 trạng thái
        statusList.add("chờ xác nhận");
        statusList.add("đang chuẩn bị");
        statusList.add("đang giao");
        statusList.add("hoàn thành");
    }

    public OrderStatusAdapter(List<String> statusList) {
        this.statusList = statusList;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context ctx = context != null ? context : parent.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_category_user, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        String status = statusList.get(position);
        String displayText = getDisplayText(status);
        holder.tvStatusName.setText(displayText);

        // Highlight selected status
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#B5CBF3"));
            holder.tvStatusName.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.tvStatusName.setTextColor(Color.parseColor("#000000"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                setSelectedPosition(position);
                listener.onStatusClick(status);
            }
        });
    }

    private String getDisplayText(String status) {
        switch (status.toLowerCase()) {
            case "chờ xác nhận":
                return "Chờ xác nhận";
            case "đang chuẩn bị":
                return "Đang chuẩn bị";
            case "đang giao":
                return "Đang giao";
            case "hoàn thành":
                return "Hoàn thành";
            default:
                return status;
        }
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatusName;
        CardView cardView;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatusName = itemView.findViewById(R.id.tvCategoryName);
            cardView = (CardView) itemView;
        }
    }
}

