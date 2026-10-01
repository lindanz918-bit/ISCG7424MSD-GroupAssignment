package com.groupproject.justeat.feature.customer;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ItemCustomerOrderBinding;

import java.util.List;

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.ViewHolder> {
    private List<Order> orderList;
    private onItemClickListener listener;
    public interface onItemClickListener {
        void onViewMenuClick(Order order, int position);
        void onDeleteClick(Order order, int position);
    }

    public CustomerOrderAdapter(List<Order> orderList, onItemClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomerOrderBinding binding = ItemCustomerOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.binding.tvOrderId.setText("#" + order.getOrderId());
        holder.binding.tvOrderStatus.setText(order.getStatus());
        holder.binding.tvOrderDate.setText(order.getBookingDate());
        holder.binding.tvOrderGuests.setText(order.getGuestCount() + " Guests (Table " + order.getTableId() + ")");

        holder.binding.btnViewMenu.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewMenuClick(order, position);
            }
        });

        holder.binding.btnDeleteOrder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(order, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ItemCustomerOrderBinding binding;
        public ViewHolder(@NonNull ItemCustomerOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}



