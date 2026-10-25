package com.groupproject.justeat.feature.staff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.RestaurantTable;
import com.groupproject.justeat.databinding.ItemStaffTableBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Staff 餐桌状态卡片。 */
public class StaffTableAdapter extends RecyclerView.Adapter<StaffTableAdapter.ViewHolder> {
    public interface Listener {
        void onChange(RestaurantTable table, boolean occupancy);
    }

    private final List<RestaurantTable> tables = new ArrayList<>();
    private final Listener listener;
    private Map<String, String> bookings = Collections.emptyMap();
    private boolean busy;

    public void setBookings(Map<String, String> data) {
        bookings = data;
        notifyDataSetChanged();
    }

    public void setBusy(boolean value) {
        busy = value;
        notifyDataSetChanged();
    }

    public StaffTableAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<RestaurantTable> data) {
        tables.clear();
        if (data != null) tables.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                ItemStaffTableBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RestaurantTable table = tables.get(position);
        String number =
                table.getTableNumber() == null || table.getTableNumber().isEmpty()
                        ? "-"
                        : table.getTableNumber();
        String status =
                table.hasValidState()
                        ? (table.getIsEnabled() ? "ENABLED" : "DISABLED")
                        : "MIGRATION NEEDED";
        holder.binding.tvStaffTableNumber.setText(
                holder.binding
                        .getRoot()
                        .getContext()
                        .getString(R.string.staff_table_number, number));
        holder.binding.tvStaffTableCapacity.setText(
                holder.binding
                                .getRoot()
                                .getContext()
                                .getString(R.string.staff_table_capacity, table.getCapacity())
                        + "\nOn-site: "
                        + (table.hasValidState()
                                ? table.getOccupancyStatus()
                                : "Unknown (missing table fields)"));
        String booking = bookings.get(table.getTableId());
        holder.binding.tvStaffTableLocation.setVisibility(
                booking == null ? View.GONE : View.VISIBLE);
        holder.binding.tvStaffTableLocation.setText(
                booking == null ? "" : "Day reservations (including completed):\n" + booking);
        StaffUi.badge(holder.binding.tvStaffTableStatus, status);
        holder.binding.btnStaffTableToggle.setEnabled(table.hasValidState() && !busy);
        holder.binding.btnStaffTableToggle.setText(
                Boolean.TRUE.equals(table.getIsEnabled()) ? "Disable bookings" : "Enable bookings");
        holder.binding.btnStaffTableToggle.setOnClickListener(v -> listener.onChange(table, false));
        holder.binding.btnStaffTableOccupancy.setVisibility(View.VISIBLE);
        holder.binding.btnStaffTableOccupancy.setText(
                "OCCUPIED".equals(table.getOccupancyStatus()) ? "Release table" : "Mark occupied");
        holder.binding.btnStaffTableOccupancy.setEnabled(table.hasValidState() && !busy);
        holder.binding.btnStaffTableOccupancy.setOnClickListener(
                v -> listener.onChange(table, true));
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemStaffTableBinding binding;

        ViewHolder(ItemStaffTableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
