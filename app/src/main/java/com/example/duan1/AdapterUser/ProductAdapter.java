package com.example.duan1.AdapterUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.duan1.R;
import com.example.duan1.model.DanhMuc;
import com.example.duan1.model.Product;
import com.example.duan1.services.ApiServices;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private List<Product> productListFull;
    private Context context;
    private OnProductClickListener listener;
    private List<com.example.duan1.model.DanhMuc> danhMucList;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
        this.danhMucList = new ArrayList<>();
    }

    public void setDanhMucList(List<DanhMuc> danhMucList) {
        this.danhMucList = danhMucList != null ? danhMucList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        this.productListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
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
        
        // Hiển thị tên danh mục
        String categoryName = "Chưa phân loại";
        if (product.getCategoryId() != null && danhMucList != null) {
            for (DanhMuc danhMuc : danhMucList) {
                if (danhMuc.getId() != null && danhMuc.getId().equals(product.getCategoryId())) {
                    categoryName = danhMuc.getName();
                    break;
                }
            }
        }
        holder.tvProductCategory.setText(categoryName);
        
        // Load ảnh sản phẩm
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imageUrl = product.getImage();
            // Nếu URL không bắt đầu bằng http, thêm base URL
            if (!imageUrl.startsWith("http")) {
                imageUrl = ApiServices.Url.replace("/api/", "") + imageUrl;
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_home);
        }

        // Click vào sản phẩm
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        // Click vào button giỏ hàng
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
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
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvProductCategory;
        ImageButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}

