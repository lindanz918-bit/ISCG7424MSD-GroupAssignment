package com.groupproject.justeat.feature.staff;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.R;
import com.groupproject.justeat.databinding.ItemStaffTableBinding;
import com.groupproject.justeat.domain.constant.TableStatus;

import java.util.ArrayList;
import java.util.List;

/** Staff 餐桌状态卡片。 */
public class StaffTableAdapter extends RecyclerView.Adapter<StaffTableAdapter.ViewHolder> {
    public interface Listener {
        void onChange(StaffTableState table, boolean occupancy);
    }

    private final List<StaffTableState> tables = new ArrayList<>();
    private final Listener listener;
    private java.util.Map<String,String> bookings=java.util.Collections.emptyMap();
    private boolean busy;
    public void setBookings(java.util.Map<String,String> data) { bookings=data; notifyDataSetChanged(); }
    public void setBusy(boolean value) { busy=value; notifyDataSetChanged(); }

    public StaffTableAdapter(Listener listener) { this.listener = listener; }

    public void submitList(List<StaffTableState> data) {
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
        StaffTableState table = tables.get(position);
        String number = table.number.isEmpty() ? "-" : table.number;
        String status = table.isMigrated() ? (table.enabled ? "ENABLED" : "DISABLED") : "MIGRATION NEEDED";
        holder.binding.tvStaffTableNumber.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_table_number, number));
        holder.binding.tvStaffTableCapacity.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_table_capacity, table.capacity) + "\nOn-site: " + (table.isMigrated() ? table.occupancy : "Unknown (missing table fields)"));
        String booking=bookings.get(table.key);
        String legacy=bookings.get(number);
        if (!table.key.equals(number) && legacy != null)
            booking=(booking==null ? "" : booking+"\n")+"Legacy table number — migration needed:\n"+legacy;
        holder.binding.tvStaffTableLocation.setVisibility(booking==null?android.view.View.GONE:android.view.View.VISIBLE);
        holder.binding.tvStaffTableLocation.setText(booking==null?"":"Day reservations (including completed):\n"+booking);
        StaffUi.badge(holder.binding.tvStaffTableStatus,status);
        holder.binding.btnStaffTableToggle.setEnabled(table.isMigrated() && !busy);
        holder.binding.btnStaffTableToggle.setText(Boolean.TRUE.equals(table.enabled) ? "Disable bookings" : "Enable bookings");
        holder.binding.btnStaffTableToggle.setOnClickListener(v -> listener.onChange(table,false));
        holder.binding.btnStaffTableOccupancy.setVisibility(android.view.View.VISIBLE);
        holder.binding.btnStaffTableOccupancy.setText("OCCUPIED".equals(table.occupancy) ? "Release table" : "Mark occupied");
        holder.binding.btnStaffTableOccupancy.setEnabled(table.isMigrated() && !busy);
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
