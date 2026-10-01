package com.groupproject.justeat.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.databinding.DialogOrderMenuDetailsBinding;
import com.groupproject.justeat.feature.customer.CustomerMyMenuAdapter;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrderMenuDetailsDialog extends BottomSheetDialogFragment {

    public static final String TAG = "OrderMenuDetailsDialog";
    private static final String ARG_ORDER_ID = "arg_order_id";

    private DialogOrderMenuDetailsBinding binding;
    private List<MenuItem> menuList;
    private CustomerMyMenuAdapter adapter;
    private DatabaseReference orderItemsRef;

    public static CustomerOrderMenuDetailsDialog newInstance(String orderId) {
        CustomerOrderMenuDetailsDialog dialog = new CustomerOrderMenuDetailsDialog();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogOrderMenuDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String orderId = getArguments() != null ? getArguments().getString(ARG_ORDER_ID, "") : "";
        binding.tvDialogTitle.setText("Items for Order #" + orderId);

        binding.btnClose.setOnClickListener(v -> dismiss());

        setupRecyclerView();
        fetchOrderMenuItems(orderId);
    }

    private void setupRecyclerView() {
        menuList = new ArrayList<>();
        // Read-only mode: pass null listener or high-level adapter configured without delete button
        adapter = new CustomerMyMenuAdapter(requireContext(), menuList, null);
        binding.rvOrderMenuItems.setAdapter(adapter);
    }

    private void fetchOrderMenuItems(String orderId) {
        if (orderId.isEmpty()) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        // Firebase node: orders/{orderId}/items
        orderItemsRef = FirebaseDatabase.getInstance()
                .getReference("OrderItems")
                .child(orderId);
        orderItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                menuList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    MenuItem item = itemSnapshot.getValue(MenuItem.class);
                    if (item != null) {
                        menuList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load items: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent ViewBinding memory leaks
    }
}