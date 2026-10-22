package com.groupproject.justeat.feature.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.RestaurantTable;
import com.groupproject.justeat.databinding.ItemStaffTableBinding;
import com.groupproject.justeat.domain.constant.TableStatus;

import java.util.ArrayList;
import java.util.List;

/** Staff 餐桌状态卡片。 */
public class StaffTableAdapter extends RecyclerView.Adapter<StaffTableAdapter.ViewHolder> {
    public interface Listener {
        void onChange(RestaurantTable table, boolean occupancy);
    }

    private final List<RestaurantTable> tables = new ArrayList<>();
    private final Listener listener;
    private java.util.Map<String,String> bookings=java.util.Collections.emptyMap();
    private boolean busy;
    public void setBookings(java.util.Map<String,String> data) { bookings=data; notifyDataSetChanged(); }
    public void setBusy(boolean value) { busy=value; notifyDataSetChanged(); }

    public StaffTableAdapter(Listener listener) { this.listener = listener; }

    public void submitList(List<RestaurantTable> data) {
        tables.clear();
        if (data != null) tables.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemStaffTableBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RestaurantTable table = tables.get(position);
        String number = table.getTableNumber() == null ? "-" : table.getTableNumber();
        String status = table.getStatus() == null ? "-" : table.getStatus();
        holder.binding.tvStaffTableNumber.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_table_number, number));
        holder.binding.tvStaffTableCapacity.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_table_capacity, table.getCapacity()));
        String booking=bookings.get(number);
        holder.binding.tvStaffTableLocation.setVisibility(booking==null?android.view.View.GONE:android.view.View.VISIBLE);
        holder.binding.tvStaffTableLocation.setText(booking==null?"":"Active bookings:\n"+booking);
        StaffUi.badge(holder.binding.tvStaffTableStatus,status);
        boolean editable = TableStatus.AVAILABLE.equalsIgnoreCase(status) || TableStatus.UNAVAILABLE.equalsIgnoreCase(status);
        holder.binding.btnStaffTableToggle.setEnabled(editable && !busy);
        holder.binding.btnStaffTableToggle.setText(TableStatus.AVAILABLE.equalsIgnoreCase(status)
                ? R.string.staff_make_unavailable : R.string.staff_make_available);
        holder.binding.btnStaffTableToggle.setOnClickListener(v -> listener.onChange(table,false));
        boolean occupied=TableStatus.OCCUPIED.equalsIgnoreCase(status) || TableStatus.RESERVED.equalsIgnoreCase(status);
        holder.binding.btnStaffTableOccupancy.setVisibility(occupied || TableStatus.AVAILABLE.equalsIgnoreCase(status)
            ? android.view.View.VISIBLE : android.view.View.GONE);
        holder.binding.btnStaffTableOccupancy.setText(occupied?"Release table":"Mark occupied");
        holder.binding.btnStaffTableOccupancy.setEnabled(!busy);
        holder.binding.btnStaffTableOccupancy.setOnClickListener(v -> listener.onChange(table,true));
    }

    @Override public int getItemCount() { return tables.size(); }


    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemStaffTableBinding binding;
        ViewHolder(ItemStaffTableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
