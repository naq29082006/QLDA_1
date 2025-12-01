package com.example.duan1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.Product;

import java.util.List;

public class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.TopProductViewHolder> {
    private List<Product> productList;
    private Context context;

    public TopProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public TopProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_product, parent, false);
        return new TopProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvProductName.setText(product.getName());
        holder.tvQuantity.setText(String.valueOf(product.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class TopProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvProductName, tvQuantity;

        public TopProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}

