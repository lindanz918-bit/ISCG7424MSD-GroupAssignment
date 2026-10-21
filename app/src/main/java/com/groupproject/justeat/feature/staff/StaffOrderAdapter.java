package com.groupproject.justeat.feature.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ItemStaffOrderBinding;
import com.groupproject.justeat.domain.constant.OrderStatus;

import java.util.ArrayList;
import java.util.List;

/** Staff 订单卡片及状态操作按钮。 */
public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.ViewHolder> {
    public interface Listener {
        void onStatusAction(Order order, String targetStatus);
    }

    private final List<Order> orders = new ArrayList<>();
    private final Listener listener;

    public StaffOrderAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Order> data) {
        orders.clear();
        if (data != null) orders.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemStaffOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        String orderId = order.getOrderId() == null ? "-" : order.getOrderId();
        String customerId = order.getCustomerId() == null ? "-" : order.getCustomerId();
        String tableId = order.getTableId() == null ? "-" : order.getTableId();
        String status = order.getStatus() == null ? "-" : order.getStatus();
        holder.binding.tvStaffOrderId.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_order_id, orderId));
        holder.binding.tvStaffOrderCustomer.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_order_customer, customerId));
        holder.binding.tvStaffOrderTable.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_order_table, tableId));
        holder.binding.tvStaffOrderStatus.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_order_status, status));
        holder.binding.btnStaffAccept.setVisibility(OrderStatus.PENDING.equals(status)
                ? android.view.View.VISIBLE : android.view.View.GONE);
        holder.binding.btnStaffReject.setVisibility(OrderStatus.PENDING.equals(status)
                ? android.view.View.VISIBLE : android.view.View.GONE);
        boolean canCancel = OrderStatus.ACCEPTED.equals(status)
                || OrderStatus.PREPARING.equals(status) || OrderStatus.READY.equals(status);
        holder.binding.btnStaffCancel.setVisibility(canCancel
                ? android.view.View.VISIBLE : android.view.View.GONE);
        holder.binding.btnStaffAccept.setOnClickListener(v -> listener.onStatusAction(order, OrderStatus.ACCEPTED));
        holder.binding.btnStaffReject.setOnClickListener(v -> listener.onStatusAction(order, OrderStatus.REJECTED));
        holder.binding.btnStaffCancel.setOnClickListener(v -> listener.onStatusAction(order, OrderStatus.CANCELLED));
    }

    @Override public int getItemCount() { return orders.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemStaffOrderBinding binding;
        ViewHolder(ItemStaffOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
