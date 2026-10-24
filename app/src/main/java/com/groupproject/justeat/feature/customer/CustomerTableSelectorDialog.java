package com.groupproject.justeat.feature.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.RestaurantTable;

import java.util.ArrayList;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.Map;
import android.widget.Toast;

public class CustomerTableSelectorDialog extends BottomSheetDialogFragment {
    private RecyclerView recyclerView;
    private CustomerTableAdapter tableAdapter;
    private List<RestaurantTable> tableList;
    private DatabaseReference databaseReference;
    private DatabaseReference reservationsReference;
    private ValueEventListener tablesListener, reservationsListener;
    private DataSnapshot tablesSnapshot, reservationsSnapshot;
    private final Map<RestaurantTable, String> tableKeys = new IdentityHashMap<>();
    private final Map<RestaurantTable, Boolean> availability = new IdentityHashMap<>();

    public static CustomerTableSelectorDialog newInstance(String date, int guests) {
        CustomerTableSelectorDialog dialog = new CustomerTableSelectorDialog();
        Bundle args = new Bundle();
        args.putString("date", date);
        args.putInt("guests", guests);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnTableSelectedListener {
        void onTableSelected(String tableKey, String tableName);
    }

    private OnTableSelectedListener listener;

    public void setOnTableSelectedListener(OnTableSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_customer_table_selector, container, false);

        recyclerView = view.findViewById(R.id.recyclerTables);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        tableList = new ArrayList<>();
        tableAdapter = new CustomerTableAdapter(tableList, availability, table -> {
            if (listener != null) {
                listener.onTableSelected(tableKeys.get(table), table.getTableNumber());
            }
            dismiss();
        });
        recyclerView.setAdapter(tableAdapter);

        fetchTablesFromFirebase();

        return view;
    }

    private void fetchTablesFromFirebase() {
        Bundle args = getArguments();
        String date = args == null ? null : args.getString("date");
        if (date == null || !date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return;
        databaseReference = FirebaseDatabase.getInstance().getReference("tables");
        reservationsReference = FirebaseDatabase.getInstance().getReference("tableReservations").child(date);
        tablesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tablesSnapshot = snapshot;
                renderTables();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showReadError();
            }
        };
        reservationsListener = new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reservationsSnapshot = snapshot;
                renderTables();
            }
            public void onCancelled(@NonNull DatabaseError error) { showReadError(); }
        };
        databaseReference.addValueEventListener(tablesListener);
        reservationsReference.addValueEventListener(reservationsListener);
    }

    private void renderTables() {
        if (getView() == null || tablesSnapshot == null || reservationsSnapshot == null) return;
        tableList.clear();
        tableKeys.clear();
        availability.clear();
        int guests = requireArguments().getInt("guests");
        for (DataSnapshot child : tablesSnapshot.getChildren()) {
            Object capacity = child.child("capacity").getValue();
            boolean available = Boolean.TRUE.equals(child.child("isEnabled").getValue())
                    && capacity instanceof Number && guests > 0 && ((Number) capacity).intValue() >= guests
                    && !reservationsSnapshot.child(child.getKey()).exists();
            // 可订性仅用于页面展示，独立于餐桌的现场状态，不写回数据库。
            RestaurantTable table = new RestaurantTable(
                    child.child("tableNumber").getValue(String.class),
                    capacity instanceof Number ? ((Number) capacity).intValue() : 0,
                    child.child("occupancyStatus").getValue(String.class));
            tableList.add(table);
            tableKeys.put(table, child.getKey());
            availability.put(table, available);
        }
        tableAdapter.notifyDataSetChanged();
    }

    private void showReadError() {
        tablesSnapshot = null;
        reservationsSnapshot = null;
        tableList.clear();
        tableKeys.clear();
        availability.clear();
        tableAdapter.notifyDataSetChanged();
        if (getContext() != null) Toast.makeText(getContext(),
                "Cannot load table availability. Please reopen.", Toast.LENGTH_LONG).show();
    }

    @Override public void onDestroyView() {
        if (databaseReference != null && tablesListener != null) databaseReference.removeEventListener(tablesListener);
        if (reservationsReference != null && reservationsListener != null) reservationsReference.removeEventListener(reservationsListener);
        tablesSnapshot = null;
        reservationsSnapshot = null;
        super.onDestroyView();
    }
}
