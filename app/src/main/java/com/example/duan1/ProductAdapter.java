package com.example.duan1;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private List<Product> productListFull;
    private Context context;
    private OnAddToCartClickListener listener;

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public void setOnAddToCartClickListener(OnAddToCartClickListener listener) {
        this.listener = listener;
    }

    // Màu sắc cho các icon sản phẩm
    private int[] colors = {
            Color.parseColor("#FFD700"), // Vàng
            Color.parseColor("#9370DB"), // Tím
            Color.parseColor("#87CEEB"), // Xanh nhạt
            Color.parseColor("#FFA500"), // Cam
            Color.parseColor("#FF69B4"), // Hồng
            Color.parseColor("#32CD32"), // Xanh lá
    };

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(product.getFormattedPrice());
        
        // Set màu cho icon (lặp lại nếu cần)
        int colorIndex = position % colors.length;
        holder.viewIcon.setBackgroundColor(colors[colorIndex]);

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(product);
            } else {
                Toast.makeText(context, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void filterList(String text) {
        productList.clear();
        if (text == null || text.trim().isEmpty()) {
            productList.addAll(productListFull);
        } else {
            text = text.toLowerCase().trim();
            for (Product product : productListFull) {
                if (product.getName().toLowerCase().contains(text)) {
                    productList.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        View viewIcon;
        TextView tvProductName, tvProductPrice;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            viewIcon = itemView.findViewById(R.id.viewIcon);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}

