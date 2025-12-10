package com.example.duan1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.duan1.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapterForThongKe extends RecyclerView.Adapter<ProductAdapterForThongKe.ProductViewHolder> {
    private List<Product> productList;
    private Context context;

    public ProductAdapterForThongKe(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
    }

    public void updateList(List<Product> newList) {
        productList.clear();
        if (newList != null) {
            productList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        
        holder.tvRank.setText("#" + (position + 1));
        holder.tvProductName.setText(product.getName() != null ? product.getName() : "N/A");
        
        // Hiển thị số lượng nếu có
        if (product.getQuantity() > 0) {
            holder.tvQuantity.setText("Đã bán: " + product.getQuantity());
            holder.tvQuantity.setVisibility(View.VISIBLE);
        } else {
            holder.tvQuantity.setVisibility(View.GONE);
        }
        
        // Hiển thị giá
        holder.tvRevenue.setText("Giá: " + product.getFormattedPrice());
        
        // Load image
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imageUrl = com.example.duan1.services.ApiServices.Url + product.getImage();
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_home);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvProductName, tvQuantity, tvRevenue;
        ImageView imgProduct;
        CardView cardView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}

