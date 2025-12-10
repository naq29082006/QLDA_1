package com.example.duan1.AdapterAdmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.R;
import com.example.duan1.model.Voucher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> voucherList;
    private List<Voucher> voucherListFull;
    private OnVoucherClickListener listener;

    public interface OnVoucherClickListener {
        void onEditClick(Voucher voucher, int position);
        void onDeleteClick(Voucher voucher, int position);
    }

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
        this.voucherListFull = new ArrayList<>(voucherList);
    }

    public void setOnVoucherClickListener(OnVoucherClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        if (position >= voucherList.size()) {
            android.util.Log.e("VoucherAdapter", "Position out of bounds: " + position + ", list size: " + voucherList.size());
            return;
        }
        
        Voucher voucher = voucherList.get(position);
        android.util.Log.d("VoucherAdapter", "Binding voucher at position " + position + ": " + voucher.getVoucherCode());
        
        holder.tvVoucherCode.setText(voucher.getVoucherCode());
        holder.tvTitle.setText(voucher.getTitle());
        holder.tvDiscount.setText("Giảm " + (int)voucher.getDiscountPercentage() + "%");
        
        // Format dates
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            if (voucher.getStartDate() != null && !voucher.getStartDate().isEmpty()) {
                String startDateStr = voucher.getStartDate();
                // Handle ISO format from MongoDB (e.g., "2024-12-01T00:00:00.000Z")
                if (startDateStr.contains("T")) {
                    startDateStr = startDateStr.substring(0, startDateStr.indexOf("T"));
                }
                Date startDate = inputSdf.parse(startDateStr);
                if (startDate != null) {
                    holder.tvStartDate.setText(sdf.format(startDate));
                } else {
                    holder.tvStartDate.setText(startDateStr);
                }
            } else {
                holder.tvStartDate.setText("N/A");
            }
            
            if (voucher.getEndDate() != null && !voucher.getEndDate().isEmpty()) {
                String endDateStr = voucher.getEndDate();
                if (endDateStr.contains("T")) {
                    endDateStr = endDateStr.substring(0, endDateStr.indexOf("T"));
                }
                Date endDate = inputSdf.parse(endDateStr);
                if (endDate != null) {
                    holder.tvEndDate.setText(sdf.format(endDate));
                } else {
                    holder.tvEndDate.setText(endDateStr);
                }
            } else {
                holder.tvEndDate.setText("N/A");
            }
        } catch (Exception e) {
            // Fallback: display raw date string
            if (voucher.getStartDate() != null) {
                String startDateStr = voucher.getStartDate();
                if (startDateStr.contains("T")) {
                    startDateStr = startDateStr.substring(0, startDateStr.indexOf("T"));
                }
                holder.tvStartDate.setText(startDateStr);
            } else {
                holder.tvStartDate.setText("N/A");
            }
            
            if (voucher.getEndDate() != null) {
                String endDateStr = voucher.getEndDate();
                if (endDateStr.contains("T")) {
                    endDateStr = endDateStr.substring(0, endDateStr.indexOf("T"));
                }
                holder.tvEndDate.setText(endDateStr);
            } else {
                holder.tvEndDate.setText("N/A");
            }
        }
        
        // Status - check thời gian thực tế
        boolean isCurrentlyActive = false;
        if (voucher.isActive() && voucher.getStartDate() != null && voucher.getEndDate() != null) {
            try {
                java.util.Date now = new java.util.Date();
                String startDateStr = voucher.getStartDate();
                String endDateStr = voucher.getEndDate();
                
                // Parse date string (có thể là ISO format hoặc yyyy-MM-dd)
                java.util.Date startDate = null;
                java.util.Date endDate = null;
                
                // Thử parse ISO format trước
                try {
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    startDate = isoFormat.parse(startDateStr);
                    endDate = isoFormat.parse(endDateStr);
                } catch (Exception e1) {
                    // Thử parse format yyyy-MM-dd
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        startDate = dateFormat.parse(startDateStr);
                        endDate = dateFormat.parse(endDateStr);
                    } catch (Exception e2) {
                        // Nếu cả 2 đều fail, thử parse trực tiếp
                        startDate = new java.util.Date(startDateStr);
                        endDate = new java.util.Date(endDateStr);
                    }
                }
                
                if (startDate != null && endDate != null) {
                    // Set end date to end of day
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(endDate);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    cal.set(java.util.Calendar.MINUTE, 59);
                    cal.set(java.util.Calendar.SECOND, 59);
                    endDate = cal.getTime();
                    
                    isCurrentlyActive = now.compareTo(startDate) >= 0 && now.compareTo(endDate) <= 0;
                }
            } catch (Exception e) {
                android.util.Log.e("VoucherAdapter", "Error checking date: " + e.getMessage());
                // Fallback: chỉ check is_active flag
                isCurrentlyActive = voucher.isActive();
            }
        }
        
        if (isCurrentlyActive) {
            holder.tvStatus.setText("Đang hoạt động");
            holder.tvStatus.setBackgroundColor(0xFF4CAF50);
        } else {
            holder.tvStatus.setText("Không hoạt động");
            holder.tvStatus.setBackgroundColor(0xFF757575);
        }
        
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(voucher, position);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(voucher, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public void updateList(List<Voucher> newList) {
        voucherList.clear();
        voucherList.addAll(newList);
        voucherListFull.clear();
        voucherListFull.addAll(newList);
        android.util.Log.d("VoucherAdapter", "updateList called with " + newList.size() + " items");
        notifyDataSetChanged();
    }

    public void filterList(String searchText) {
        voucherList.clear();
        if (searchText.isEmpty()) {
            voucherList.addAll(voucherListFull);
        } else {
            String searchLower = searchText.toLowerCase();
            for (Voucher voucher : voucherListFull) {
                if (voucher.getVoucherCode().toLowerCase().contains(searchLower) ||
                    voucher.getTitle().toLowerCase().contains(searchLower)) {
                    voucherList.add(voucher);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvVoucherCode, tvTitle, tvDiscount, tvStartDate, tvEndDate, tvStatus;
        ImageView btnEdit, btnDelete;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVoucherCode = itemView.findViewById(R.id.tvVoucherCode);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

