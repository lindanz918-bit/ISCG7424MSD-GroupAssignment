package com.groupproject.justeat.feature.customer;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ItemCustomerOrderBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.ViewHolder> {
    private List<Order> orderList;
    private onItemClickListener listener;
    public interface onItemClickListener {
        void onViewMenuClick(Order order, int position);
        void onDeleteClick(Order order, int position);
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
        holder.binding.tvOrderDate.setText(order.getBookingDate());
        holder.binding.tvOrderGuests.setText(order.getGuestCount() + " Guests (Table " + order.getTableId() + ")");

        boolean isPast = isDateBeforeToday(order.getBookingDate());
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";

        if (isPast || "CANCELLED".equals(status)) {
            holder.binding.btnDeleteOrder.setText("Delete");

            holder.binding.btnDeleteOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(order, position);
                }
            });
        } else {
            holder.binding.btnDeleteOrder.setText("Cancel");
            holder.binding.btnDeleteOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelClick(order, position);
                }
            });
        }

        holder.binding.btnViewMenu.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewMenuClick(order, position);
            }
        });


    }

    private boolean isDateBeforeToday(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date orderDate = sdf.parse(dateStr);
            if (orderDate == null) return false;

            Calendar calOrder = Calendar.getInstance();
            calOrder.setTime(orderDate);
            calOrder.set(Calendar.HOUR_OF_DAY, 0);
            calOrder.set(Calendar.MINUTE, 0);
            calOrder.set(Calendar.SECOND, 0);
            calOrder.set(Calendar.MILLISECOND, 0);

            Calendar calToday = Calendar.getInstance();
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);

            return calOrder.before(calToday);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
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



