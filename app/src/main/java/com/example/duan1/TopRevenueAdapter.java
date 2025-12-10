package com.example.duan1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.TopRevenue;

import java.util.ArrayList;
import java.util.List;

public class TopRevenueAdapter extends RecyclerView.Adapter<TopRevenueAdapter.RevenueViewHolder> {
    private List<TopRevenue> revenueList;
    private Context context;

    public TopRevenueAdapter(Context context) {
        this.context = context;
        this.revenueList = new ArrayList<>();
    }

    public void updateList(List<TopRevenue> newList) {
        revenueList.clear();
        revenueList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_revenue, parent, false);
        return new RevenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RevenueViewHolder holder, int position) {
        TopRevenue revenue = revenueList.get(position);
        
        Log.d("TopRevenueAdapter", "Binding revenue " + position + " - Date: " + revenue.getDate() + ", Revenue: " + revenue.getTotalRevenue() + ", OrderCount: " + revenue.getOrderCount());
        
        // Nếu là item tổng (có chứa "Tổng" trong date), không hiển thị rank
        if (revenue.getDate() != null && revenue.getDate().contains("Tổng")) {
            holder.tvRank.setText("Tổng");
        } else {
            holder.tvRank.setText("#" + (position + 1));
        }
        
        holder.tvDate.setText(revenue.getDate());
        holder.tvRevenue.setText(revenue.getFormattedRevenue());
        holder.tvOrderCount.setText(revenue.getOrderCount() + " đơn hàng");
        
        Log.d("TopRevenueAdapter", "Set orderCount text: " + (revenue.getOrderCount() + " đơn hàng"));
    }

    @Override
    public int getItemCount() {
        return revenueList.size();
    }

    public class RevenueViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvDate, tvRevenue, tvOrderCount;
        CardView cardView;

        public RevenueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
            tvOrderCount = itemView.findViewById(R.id.tvOrderCount);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}

