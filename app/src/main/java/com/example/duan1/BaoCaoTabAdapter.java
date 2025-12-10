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

public class BaoCaoTabAdapter extends RecyclerView.Adapter<BaoCaoTabAdapter.TabViewHolder> {
    private List<String> tabList;
    private Context context;
    private OnTabClickListener listener;
    private int selectedPosition = 0;

    public interface OnTabClickListener {
        void onTabClick(String tab);
    }

    public void setOnTabClickListener(OnTabClickListener listener) {
        this.listener = listener;
    }

    public BaoCaoTabAdapter(Context context) {
        this.context = context;
        this.tabList = new ArrayList<>();
        tabList.add("Top Khách Hàng");
        tabList.add("Top Sản Phẩm");
        tabList.add("Top Doanh Thu");
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
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_user, parent, false);
        return new TabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        String tab = tabList.get(position);
        holder.tvTabName.setText(tab);

        // Highlight selected tab
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#B5CBF3"));
            holder.tvTabName.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.tvTabName.setTextColor(Color.parseColor("#000000"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                setSelectedPosition(position);
                listener.onTabClick(tab);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tabList.size();
    }

    public class TabViewHolder extends RecyclerView.ViewHolder {
        TextView tvTabName;
        CardView cardView;

        public TabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTabName = itemView.findViewById(R.id.tvCategoryName);
            cardView = (CardView) itemView;
        }
    }
}

