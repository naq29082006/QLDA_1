package com.example.duan1.AdapterUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.duan1.R;
import com.example.duan1.model.CartItem;
import com.example.duan1.model.Product;
import com.example.duan1.services.ApiServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItemList;
    private Context context;
    private Set<String> selectedItems;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onItemSelectedChanged();
        void onQuantityChanged(CartItem cartItem, int newQuantity);
        void onItemDeleted(CartItem cartItem);
    }

    public void setOnCartItemChangeListener(OnCartItemChangeListener listener) {
        this.listener = listener;
    }

    public CartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList != null ? cartItemList : new ArrayList<>();
        this.selectedItems = new HashSet<>();
    }

    public void updateList(List<CartItem> newList) {
        this.cartItemList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<CartItem> getSelectedItems() {
        List<CartItem> selected = new ArrayList<>();
        for (CartItem item : cartItemList) {
            if (selectedItems.contains(item.getId())) {
                selected.add(item);
            }
        }
        return selected;
    }

    public double getSelectedTotal() {
        double total = 0;
        for (CartItem item : cartItemList) {
            if (selectedItems.contains(item.getId())) {
                total += item.getSubtotal();
            }
        }
        return total;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        Product product = cartItem.getProduct();

        if (product != null) {
            holder.tvProductName.setText(product.getName());
            holder.tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
            // Chỉ hiển thị tổng tiền (subtotal = giá x số lượng)
            holder.tvSubtotal.setText(cartItem.getFormattedSubtotal());

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
        }

        // Checkbox
        holder.checkboxSelect.setChecked(selectedItems.contains(cartItem.getId()));
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.add(cartItem.getId());
            } else {
                selectedItems.remove(cartItem.getId());
            }
            if (listener != null) {
                listener.onItemSelectedChanged();
            }
        });

        // Tăng số lượng
        holder.btnTang.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() + 1;
            if (listener != null) {
                listener.onQuantityChanged(cartItem, newQuantity);
            }
        });

        // Giảm số lượng
        holder.btnGiam.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                int newQuantity = cartItem.getQuantity() - 1;
                if (listener != null) {
                    listener.onQuantityChanged(cartItem, newQuantity);
                }
            }
        });

        // Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemDeleted(cartItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxSelect;
        ImageView imgProduct;
        TextView tvProductName, tvQuantity, tvSubtotal;
        Button btnTang, btnGiam;
        ImageButton btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            btnTang = itemView.findViewById(R.id.btnTang);
            btnGiam = itemView.findViewById(R.id.btnGiam);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

