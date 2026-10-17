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

public class CustomerTableSelectorDialog extends BottomSheetDialogFragment {
    private RecyclerView recyclerView;
    private CustomerTableAdapter tableAdapter;
    private List<RestaurantTable> tableList;
    private DatabaseReference databaseReference;

    public interface OnTableSelectedListener {
        void onTableSelected(String tableName);
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
        tableAdapter = new CustomerTableAdapter(tableList, table -> {
            if (listener != null) {
                listener.onTableSelected(table.getTableNumber());
            }
            dismiss();
        });
        recyclerView.setAdapter(tableAdapter);

        fetchTablesFromFirebase();

        return view;
    }

    private void fetchTablesFromFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("tables");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tableList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RestaurantTable table = dataSnapshot.getValue(RestaurantTable.class);
                    if (table != null) {
                        tableList.add(table);
                    }
                }
                tableAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // error process
            }
        });
    }

}