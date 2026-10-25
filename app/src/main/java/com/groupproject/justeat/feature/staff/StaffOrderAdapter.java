package com.groupproject.justeat.feature.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ItemStaffOrderBinding;
import com.groupproject.justeat.domain.constant.OrderStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Staff 订单卡片及状态操作按钮。 */
public class StaffOrderAdapter extends RecyclerView.Adapter<StaffOrderAdapter.ViewHolder> {
    public interface Listener {
        void onStatusAction(Order order, String targetStatus);
    }

    private final List<Order> orders = new ArrayList<>();
    private final Listener listener;
    private final Map<String, String> names = new HashMap<>();
    private final Set<String> requested = new HashSet<>();
    private boolean busy;
    private final Map<String, String> tableNames = new HashMap<>();
    private final Set<String> tableRequests = new HashSet<>();

    public void setBusy(boolean value) {
        busy = value;
        notifyDataSetChanged();
    }

    public StaffOrderAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Order> data) {
        orders.clear();
        if (data != null) orders.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ItemStaffOrderBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        String orderId = order.getOrderId() == null ? "-" : order.getOrderId();
        String customerId = order.getCustomerId() == null ? "-" : order.getCustomerId();
        if (!"-".equals(customerId) && requested.add(customerId)) {
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(customerId)
                    .get()
                    .addOnSuccessListener(
                            s -> {
                                String name =
                                        (s.child("firstName").getValue(String.class) == null
                                                        ? ""
                                                        : s.child("firstName")
                                                                .getValue(String.class))
                                                + " "
                                                + (s.child("lastName").getValue(String.class)
                                                                == null
                                                        ? ""
                                                        : s.child("lastName")
                                                                .getValue(String.class));
                                if (!name.trim().isEmpty()) names.put(customerId, name.trim());
                                notifyDataSetChanged();
                            });
        }
        String tableId = order.getTableId() == null ? "-" : order.getTableId();
        if (validKey(tableId) && tableRequests.add(tableId)) {
            FirebaseDatabase.getInstance()
                    .getReference("tables")
                    .child(tableId)
                    .child("tableNumber")
                    .get()
                    .addOnSuccessListener(
                            s -> {
                                String name = s.getValue(String.class);
                                if (name != null) {
                                    tableNames.put(tableId, name);
                                    notifyDataSetChanged();
                                }
                            });
        }
        String status = order.getStatus() == null ? "-" : order.getStatus();
        holder.binding.tvStaffOrderId.setText(
                holder.binding.getRoot().getContext().getString(R.string.staff_order_id, orderId));
        holder.binding.tvStaffOrderCustomer.setText(names.getOrDefault(customerId, customerId));
        holder.binding.tvStaffOrderTable.setText(
                holder.binding
                        .getRoot()
                        .getContext()
                        .getString(
                                R.string.staff_ui_booking,
                                tableNames.getOrDefault(tableId, tableId),
                                order.getGuestCount(),
                                order.getBookingDate() == null
                                        ? "Date not provided"
                                        : order.getBookingDate()));
        holder.binding
                .getRoot()
                .setOnClickListener(
                        v ->
                                v.getContext()
                                        .startActivity(
                                                new android.content.Intent(
                                                                v.getContext(),
                                                                StaffOrderDetailsActivity.class)
                                                        .putExtra("orderId", order.getOrderId())));
        holder.binding.btnStaffOrderDetails.setOnClickListener(
                v -> holder.binding.getRoot().performClick());
        StaffUi.badge(holder.binding.tvStaffOrderStatus, status);
        holder.binding.btnStaffCancel.setTextColor(
                holder.binding.getRoot().getContext().getColor(R.color.staff_error));
        holder.binding.btnStaffAccept.setVisibility(
                OrderStatus.PENDING.equals(status) ? View.VISIBLE : View.GONE);
        holder.binding.btnStaffReject.setVisibility(
                OrderStatus.PENDING.equals(status) ? View.VISIBLE : View.GONE);
        boolean canCancel =
                OrderStatus.ACCEPTED.equals(status)
                        || OrderStatus.PREPARING.equals(status)
                        || OrderStatus.READY.equals(status);
        holder.binding.btnStaffCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        holder.binding.btnStaffAccept.setOnClickListener(
                v -> listener.onStatusAction(order, OrderStatus.ACCEPTED));
        holder.binding.btnStaffAccept.setEnabled(!busy);
        holder.binding.btnStaffReject.setEnabled(!busy);
        holder.binding.btnStaffCancel.setEnabled(!busy);
        holder.binding.btnStaffReject.setOnClickListener(
                v -> listener.onStatusAction(order, OrderStatus.REJECTED));
        holder.binding.btnStaffCancel.setOnClickListener(
                v -> listener.onStatusAction(order, OrderStatus.CANCELLED));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemStaffOrderBinding binding;

        ViewHolder(ItemStaffOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static boolean validKey(String key) {
        if (key == null || key.isEmpty()) return false;
        for (char c : key.toCharArray()) {
            if (".#$[]/".indexOf(c) >= 0 || Character.isISOControl(c)) return false;
        }
        return true;
    }
}
