
package com.groupproject.justeat.feature.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.RestaurantTable;

import java.util.List;


public class RestaurantTableAdapter extends RecyclerView.Adapter<RestaurantTableAdapter.TableViewHolder> {

    private List<RestaurantTable> tableList;
    private List<String> tableIds;
    private OnTableClickListener listener;


    public interface OnTableClickListener {
        void onEdit(String id, RestaurantTable table);
        void onDelete(String id, RestaurantTable table);
    }

    public RestaurantTableAdapter(
            List<RestaurantTable> tableList,
            List<String> tableIds,
            OnTableClickListener listener) {

        this.tableList = tableList;
        this.tableIds = tableIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_table, parent, false);

        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TableViewHolder holder, int position) {

        RestaurantTable table = tableList.get(position);
        String id = tableIds.get(position);

        holder.txtTableNumber.setText(
                "Table number: " + table.getTableNumber()
        );

        holder.txtCapacity.setText(
                "Capacity: " + table.getCapacity()
        );

        holder.btnEditTable.setOnClickListener(v -> {
            listener.onEdit(id, table);
        });

        holder.btnDeleteTable.setOnClickListener(v -> {
            listener.onDelete(id, table);
        });
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    public static class TableViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTableNumber, txtCapacity;
        MaterialButton btnEditTable, btnDeleteTable;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTableNumber = itemView.findViewById(R.id.txtTableNumber);
            txtCapacity = itemView.findViewById(R.id.txtCapacity);

            btnEditTable = itemView.findViewById(R.id.btnEditTable);
            btnDeleteTable = itemView.findViewById(R.id.btnDeleteTable);
        }
    }
}
