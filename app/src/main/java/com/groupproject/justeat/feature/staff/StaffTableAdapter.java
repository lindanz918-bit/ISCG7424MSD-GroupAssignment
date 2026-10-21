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
        void onToggle(RestaurantTable table);
    }

    private final List<RestaurantTable> tables = new ArrayList<>();
    private final Listener listener;

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
        String location = locationFor(number);
        String status = table.getStatus() == null ? "-" : table.getStatus();
        holder.binding.tvStaffTableNumber.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_table_number, number));
        holder.binding.tvStaffTableCapacity.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_table_capacity, table.getCapacity()));
        holder.binding.tvStaffTableLocation.setText(holder.binding.getRoot().getContext()
                .getString(R.string.staff_table_location, location));
        holder.binding.tvStaffTableStatus.setText(status);
        boolean editable = TableStatus.AVAILABLE.equals(status) || TableStatus.UNAVAILABLE.equals(status);
        holder.binding.btnStaffTableToggle.setEnabled(editable);
        holder.binding.btnStaffTableToggle.setText(TableStatus.AVAILABLE.equals(status)
                ? R.string.staff_make_unavailable : R.string.staff_make_available);
        holder.binding.btnStaffTableToggle.setOnClickListener(v -> listener.onToggle(table));
    }

    @Override public int getItemCount() { return tables.size(); }

    private String locationFor(String number) {
        if ("01".equals(number)) return "Main dining";
        if ("02".equals(number)) return "Window";
        if ("03".equals(number)) return "Patio";
        return "-";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemStaffTableBinding binding;
        ViewHolder(ItemStaffTableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
