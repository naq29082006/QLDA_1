package com.example.duan1.AdapterAdmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class SanPhamAdapter extends RecyclerView.Adapter<SanPhamAdapter.ProductAdminViewHolder> {
    private List<Product> productList;
    private List<Product> productListFull;
    private List<DanhMuc> danhMucList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SanPhamAdapter(Context context, List<Product> productList, List<DanhMuc> danhMucList) {
        this.context = context;
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
        this.danhMucList = danhMucList != null ? danhMucList : new ArrayList<>();
    }

    public void setDanhMucList(List<DanhMuc> danhMucList) {
        this.danhMucList = danhMucList != null ? danhMucList : new ArrayList<>();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        this.productListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public List<Product> getProductList() {
        return productList;
    }

    @NonNull
    @Override
    public ProductAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_admin, parent, false);
        return new ProductAdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdminViewHolder holder, int position) {
        if (position < 0 || position >= productList.size()) {
            return;
        }
        
        Product product = productList.get(position);
        
        holder.tvRank.setText("#" + (position + 1));
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(product.getFormattedPrice());
        
        // Tìm tên danh mục theo categoryId
        String categoryName = "Chưa có danh mục";
        if (product.getCategoryId() != null && !danhMucList.isEmpty()) {
            for (DanhMuc danhMuc : danhMucList) {
                if (danhMuc.getId() != null && danhMuc.getId().equals(product.getCategoryId())) {
                    categoryName = danhMuc.getName() != null ? danhMuc.getName() : "Chưa có tên";
                    break;
                }
            }
        }
        holder.tvProductCategory.setText("Danh mục: " + categoryName);

        // Load ảnh nếu có
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imageUrl = product.getImage();
            // Nếu URL không bắt đầu bằng http, thêm base URL
            if (!imageUrl.startsWith("http")) {
                imageUrl = ApiServices.Url + imageUrl;
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_home);
        }

        // Lưu position vào final variable để tránh vấn đề với closure
        final int pos = position;
        
        // Click vào item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && pos >= 0 && pos < productList.size()) {
                listener.onItemClick(pos);
            }
        });
        
        holder.imgEdit.setOnClickListener(v -> {
            if (listener != null && pos >= 0 && pos < productList.size()) {
                listener.onEditClick(pos);
            }
        });

        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null && pos >= 0 && pos < productList.size()) {
                listener.onDeleteClick(pos);
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

    public class ProductAdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        ImageView imgProduct, imgEdit, imgDelete;
        TextView tvProductName, tvProductPrice, tvProductCategory;

        public ProductAdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}

