package com.example.duan1.AdapterUser;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.R;
import com.example.duan1.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<DanhMuc> categoryList;
    private Context context;
    private OnCategoryClickListener listener;
    private int selectedPosition = 0; // Mặc định chọn "All" (position 0)

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryId); // null = All
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public CategoryAdapter(Context context, List<DanhMuc> categoryList) {
        this.context = context;
        this.categoryList = categoryList != null ? categoryList : new ArrayList<>();
    }

    public void updateList(List<DanhMuc> newList) {
        this.categoryList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
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
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_user, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String categoryName;
        String categoryId;
        
        if (position == 0) {
            // Item "All"
            categoryName = "All";
            categoryId = null;
        } else {
            // Item danh mục thực
            DanhMuc category = categoryList.get(position - 1);
            categoryName = category.getName();
            categoryId = category.getId();
        }
        
        holder.tvCategoryName.setText(categoryName);

        // Highlight selected category
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#B5CBF3"));
            holder.tvCategoryName.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.tvCategoryName.setTextColor(Color.parseColor("#000000"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                setSelectedPosition(position);
                listener.onCategoryClick(categoryId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size() + 1; // +1 cho item "All"
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        CardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            cardView = (CardView) itemView;
        }
    }
}

