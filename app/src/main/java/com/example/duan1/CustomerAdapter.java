package com.example.duan1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    private List<Customer> customerList;
    private List<Customer> customerListFull; // để search
    private Context context;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);

        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CustomerAdapter(Context context, List<Customer> customerList) {
        this.context = context;
        this.customerList = customerList;


        this.customerListFull = new ArrayList<>(customerList);
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customerList.get(position);

        holder.tvName.setText(customer.getName());
        holder.tvEmail.setText(customer.getEmail());
        holder.tvPhone.setText(customer.getPhone());
        holder.tvAddress.setText(customer.getAddress());
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }



    public void filterList(String text) {
        customerList.clear();

        if (text == null || text.trim().isEmpty()) {
            customerList.addAll(customerListFull);
        } else {
            text = text.toLowerCase().trim();
            for (Customer c : customerListFull) {
                if (c.getName().toLowerCase().contains(text)) {
                    customerList.add(c);
                }
            }
        }

        notifyDataSetChanged();
    }



    public class CustomerViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail, tvPhone, tvAddress;
        ImageView btnEdit, btnDelete;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.txtName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.txtPhone);
            tvAddress = itemView.findViewById(R.id.txtAddress);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getAdapterPosition());
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getAdapterPosition());
                }
            });
        }
    }
}