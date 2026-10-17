package com.groupproject.justeat.feature.customer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ActivityCustomerOrdersBinding;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.groupproject.justeat.R;
import com.groupproject.justeat.feature.customer.CustomerOrderAdapter.onItemClickListener;
import com.groupproject.justeat.ui.dialog.CustomerOrderMenuDetailsDialog;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrdersActivity extends BaseActivity<ActivityCustomerOrdersBinding> {
    private CustomerOrderAdapter adapter;
    List<Order> orderList;
    private DatabaseReference ordersRef;

    @Override
    protected ActivityCustomerOrdersBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCustomerOrdersBinding.inflate(inflater);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_customer_orders);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        orderList = new ArrayList<>();
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        initView();
        initData();
    }
    private void initView() {
        adapter = new CustomerOrderAdapter(orderList, new onItemClickListener() {
            @Override
            public void onViewMenuClick(Order order, int position) {
                CustomerOrderMenuDetailsDialog dialog = CustomerOrderMenuDetailsDialog.newInstance(order.getOrderId());
                dialog.show(getSupportFragmentManager(), CustomerOrderMenuDetailsDialog.TAG);
            }

            @Override
            public void onDeleteClick(Order order, int position) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(CustomerOrdersActivity.this)
                        .setTitle("Delete Order")
                        .setMessage("Are you sure you want to delete order #" + order.getOrderId() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            Order selectedOrder = orderList.get(position);
                            ordersRef.child(selectedOrder.getOrderId()).removeValue();
                            orderList.remove(position);
                            adapter.notifyItemRemoved(position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onCancelClick(Order order, int position) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(CustomerOrdersActivity.this)
                        .setTitle("Cancel Order")
                        .setMessage("Are you sure you want to cancel order #" + order.getOrderId() + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Order selectedOrder = orderList.get(position);
                            selectedOrder.setStatus("CANCELLED");
                            ordersRef.child(selectedOrder.getOrderId()).setValue(selectedOrder);
                            adapter.notifyItemRemoved(position);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        getBinding().rvOrders.setAdapter(adapter);

        getBinding().bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, CustomerDashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_my_menu) {
                startActivity(new Intent(this, CustomerMyMenuActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(this, CustomerAccountActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        getBinding().bottomNavigation.setSelectedItemId(R.id.nav_orders);

    }
    private void initData() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Order order = childSnapshot.getValue(Order.class);
                    if (order != null) {
                        orderList.add(order);
                        Log.d("FirebaseDebug", "Parsed Order: " + order.getOrderId());
                    } else {
                        Log.e("FirebaseDebug", "Failed to parse snapshot: " + childSnapshot.toString());
                    }
                }
                Log.d("FirebaseDebug", "Total orders in list: " + orderList.size());
                getBinding().layoutEmptyState.setVisibility(orderList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Read data from Firebase Error: " + error.getMessage());
            }
        });
    }
}