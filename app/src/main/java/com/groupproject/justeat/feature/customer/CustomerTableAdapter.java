package com.groupproject.justeat.feature.customer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.RestaurantTable;

import java.util.List;

public class CustomerTableAdapter extends RecyclerView.Adapter<CustomerTableAdapter.TableViewHolder> {
    private List<RestaurantTable> tableList;
    private OnTableClickListener listener;

    public interface OnTableClickListener {
        void onTableClick(RestaurantTable table);
    }

    public CustomerTableAdapter(List<RestaurantTable> tableList, OnTableClickListener listener) {
        this.tableList = tableList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomerTableAdapter.TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_card, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerTableAdapter.TableViewHolder holder, int position) {
        RestaurantTable table = tableList.get(position);
        holder.tvTableNumber.setText(table.getTableNumber());
        holder.tvCapacity.setText("Capacity: " + table.getCapacity() + " Guests");
        holder.tvStatus.setText(table.getStatus());

        if ("Available".equalsIgnoreCase(table.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#E65100"));
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.6f);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && "Available".equalsIgnoreCase(table.getStatus())) {
                listener.onTableClick(table);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList != null ? tableList.size() : 0;
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTableNumber, tvCapacity, tvStatus;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableNumber = itemView.findViewById(R.id.tvTableNumber);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

    }
}
