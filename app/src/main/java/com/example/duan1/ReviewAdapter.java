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

import com.example.duan1.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList;
    private List<Review> reviewListFull;
    private Context context;
    private OnReviewClickListener listener;

    public interface OnReviewClickListener {
        void onReviewClick(Review review);
    }

    public void setOnReviewClickListener(OnReviewClickListener listener) {
        this.listener = listener;
    }

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList != null ? reviewList : new ArrayList<>();
        this.reviewListFull = new ArrayList<>(this.reviewList);
    }

    public void updateList(List<Review> newList) {
        this.reviewList = newList != null ? newList : new ArrayList<>();
        this.reviewListFull = new ArrayList<>(this.reviewList);
        notifyDataSetChanged();
    }

    public void filterList(String text) {
        reviewList.clear();
        if (text == null || text.trim().isEmpty()) {
            reviewList.addAll(reviewListFull);
        } else {
            text = text.toLowerCase().trim();
            for (Review review : reviewListFull) {
                String comment = review.getComment() != null ? review.getComment().toLowerCase() : "";
                String orderId = review.getOrderId() != null ? review.getOrderId().toLowerCase() : "";
                if (comment.contains(text) || orderId.contains(text)) {
                    reviewList.add(review);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        
        holder.tvRank.setText("#" + (position + 1));
        
        // Hiển thị rating (sao)
        int rating = review.getRating();
        updateStars(holder.star1, holder.star2, holder.star3, holder.star4, holder.star5, rating);
        
        // Hiển thị comment
        String comment = review.getComment();
        if (comment == null || comment.trim().isEmpty()) {
            holder.tvComment.setText("Không có bình luận");
            holder.tvComment.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            holder.tvComment.setText(comment);
            holder.tvComment.setTextColor(context.getResources().getColor(android.R.color.black));
        }
        
        // Hiển thị order ID (có thể là string hoặc object)
        String orderId = "N/A";
        if (review.getOrderId() != null) {
            orderId = review.getOrderId();
            // Nếu order_id là object (populated), có thể cần parse thêm
            // Hiện tại giả sử API trả về order_id dạng string hoặc object có field order_id
        }
        holder.tvOrderId.setText("Mã đơn: " + orderId);
        
        // Hiển thị ngày tạo
        String createdAt = review.getCreatedAt();
        if (createdAt != null && !createdAt.isEmpty()) {
            // Format date nếu cần
            holder.tvDate.setText(createdAt);
        } else {
            holder.tvDate.setText("");
        }

        // Xử lý click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewClick(review);
            }
        });
    }

    private void updateStars(ImageView star1, ImageView star2, ImageView star3, ImageView star4, ImageView star5, int rating) {
        star1.setImageResource(rating >= 1 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        star2.setImageResource(rating >= 2 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        star3.setImageResource(rating >= 3 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        star4.setImageResource(rating >= 4 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
        star5.setImageResource(rating >= 5 ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvOrderId, tvComment, tvDate;
        ImageView star1, star2, star3, star4, star5;
        CardView cardView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvRank = itemView.findViewById(R.id.tvRank);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }
}

