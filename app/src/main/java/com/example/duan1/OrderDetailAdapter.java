package com.example.duan1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.duan1.model.OrderDetail;
import com.example.duan1.model.Product;
import com.example.duan1.services.ApiServices;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
    private List<OrderDetail> orderDetailList;
    private Context context;

    public OrderDetailAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList != null ? orderDetailList : new ArrayList<>();
    }

    public void updateList(List<OrderDetail> newList) {
        this.orderDetailList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetailList.get(position);
        
        // Lấy thông tin sản phẩm
        Product product = orderDetail.getProduct();
        if (product != null) {
            holder.tvProductName.setText(product.getName());
            holder.tvProductPrice.setText(product.getFormattedPrice());
            
            // Load ảnh sản phẩm
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                String imageUrl = product.getImage();
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
        } else {
            holder.tvProductName.setText("Sản phẩm không xác định");
            holder.tvProductPrice.setText(orderDetail.getFormattedPrice());
            holder.imgProduct.setImageResource(R.drawable.ic_home);
        }
        
        // Hiển thị số lượng
        holder.tvQuantity.setText("Số lượng: " + orderDetail.getQuantity());
        
        // Hiển thị tổng tiền
        holder.tvSubtotal.setText(orderDetail.getFormattedSubtotal());
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    public class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvQuantity, tvSubtotal;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }
    }
}

