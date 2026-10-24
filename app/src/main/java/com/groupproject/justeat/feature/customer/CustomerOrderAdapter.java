package com.groupproject.justeat.feature.customer;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ItemCustomerOrderBinding;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.ViewHolder> {
    private List<Order> orderList;
    private onItemClickListener listener;
    private Map<String, String> tableNumbers = new HashMap<>();

    public void setTableNumbers(Map<String, String> numbers) {
        tableNumbers = numbers;
        notifyDataSetChanged();
    }
    public interface onItemClickListener {
        void onViewMenuClick(Order order, int position);
        void onCancelClick(Order order, int position);
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
        String canonical = CustomerBookingFields.canonicalDate(order.getBookingDate());
        String displayDate = canonical == null ? order.getBookingDate()
                : canonical.substring(8, 10) + "/" + canonical.substring(5, 7) + "/" + canonical.substring(0, 4);
        holder.binding.tvOrderDate.setText(displayDate);
        String number = tableNumbers.containsKey(order.getTableId())
                ? tableNumbers.get(order.getTableId()) : order.getTableId();
        holder.binding.tvOrderGuests.setText(order.getGuestCount() + " Guests (Table " + number + ")");

        boolean cancellable = CustomerBookingFields.canCancel(order.getStatus());
        holder.binding.btnDeleteOrder.setText("Cancel");
        holder.binding.btnDeleteOrder.setEnabled(cancellable);
        holder.binding.btnDeleteOrder.setVisibility(cancellable ? android.view.View.VISIBLE : android.view.View.GONE);
        holder.binding.btnDeleteOrder.setOnClickListener(v -> {
            if (listener != null && CustomerBookingFields.canCancel(order.getStatus())) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onCancelClick(order, currentPosition);
                }
            }
        });

        holder.binding.btnViewMenu.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewMenuClick(order, position);
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



