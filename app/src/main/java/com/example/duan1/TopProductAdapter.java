package com.example.duan1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.duan1.model.TopProduct;

import java.util.ArrayList;
import java.util.List;

public class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.ProductViewHolder> {
    private List<TopProduct> productList;
    private Context context;

    public TopProductAdapter(Context context) {
        this.context = context;
        this.productList = new ArrayList<>();
    }

    public void updateList(List<TopProduct> newList) {
        productList.clear();
        productList.addAll(newList);
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
        TopProduct product = productList.get(position);
        
        Log.d("TopProductAdapter", "Binding product " + position + " - TotalQuantity: " + product.getTotalQuantity() + ", TotalRevenue: " + product.getTotalRevenue());
        
        holder.tvRank.setText("#" + (position + 1));
        holder.tvProductName.setText(product.getProductName() != null ? product.getProductName() : "N/A");
        holder.tvQuantity.setText("Đã bán: " + product.getTotalQuantity());
        holder.tvRevenue.setText("Doanh thu: " + product.getFormattedRevenue());
        
        // Debug: Log để kiểm tra TextView có được set không
        Log.d("TopProductAdapter", "Set quantity text: " + ("Đã bán: " + product.getTotalQuantity()));
        Log.d("TopProductAdapter", "Set revenue text: " + ("Doanh thu: " + product.getFormattedRevenue()));
        
        // Load image
        if (product.getProductImage() != null && !product.getProductImage().isEmpty()) {
            String imagePath = product.getProductImage();
            // Nếu image path đã bắt đầu bằng / thì không cần thêm /
            if (imagePath.startsWith("/")) {
                imagePath = imagePath.substring(1);
            }
            String imageUrl = com.example.duan1.services.ApiServices.Url + imagePath;
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
