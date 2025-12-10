package com.example.duan1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.model.TopCustomer;

import java.util.ArrayList;
import java.util.List;

public class TopCustomerAdapter extends RecyclerView.Adapter<TopCustomerAdapter.CustomerViewHolder> {
    private List<TopCustomer> customerList;
    private Context context;

    public TopCustomerAdapter(Context context) {
        this.context = context;
        this.customerList = new ArrayList<>();
    }

    public void updateList(List<TopCustomer> newList) {
        customerList.clear();
        customerList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        TopCustomer customer = customerList.get(position);
        
        Log.d("TopCustomerAdapter", "Binding customer " + position + " - OrderCount: " + customer.getOrderCount() + ", TotalSpent: " + customer.getTotalSpent());
        
        holder.tvRank.setText("#" + (position + 1));
        holder.tvCustomerName.setText(customer.getUserName() != null ? customer.getUserName() : "N/A");
        holder.tvCustomerEmail.setText(customer.getUserEmail() != null ? customer.getUserEmail() : "N/A");
        holder.tvTotalSpent.setText(customer.getFormattedTotalSpent());
        holder.tvOrderCount.setText(customer.getOrderCount() + " đơn hàng");
        
        // Debug: Log để kiểm tra TextView có được set không
        Log.d("TopCustomerAdapter", "Set orderCount text: " + (customer.getOrderCount() + " đơn hàng"));
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvCustomerName, tvCustomerEmail, tvTotalSpent, tvOrderCount;
        CardView cardView;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerEmail = itemView.findViewById(R.id.tvCustomerEmail);
            tvTotalSpent = itemView.findViewById(R.id.tvTotalSpent);
            tvOrderCount = itemView.findViewById(R.id.tvOrderCount);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}

