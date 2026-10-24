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
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;

public class CustomerOrdersActivity extends BaseActivity<ActivityCustomerOrdersBinding> {
    private CustomerOrderAdapter adapter;
    List<Order> orderList;
    private DatabaseReference ordersRef;
    private Query ownOrders;
    private ValueEventListener ordersListener, tablesListener;
    private DatabaseReference tablesRef;
    private final Set<String> cancelling = new HashSet<>();

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
            public void onCancelClick(Order order, int position) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(CustomerOrdersActivity.this)
                        .setTitle("Cancel Order")
                        .setMessage("Cancel order #" + order.getOrderId()
                                + " and release its table reservation for that date? The order record will be kept.")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            cancelBooking(order.getOrderId());
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
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }
        ownOrders = ordersRef.orderByChild("customerId").equalTo(uid);
        ordersListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isDestroyed()) return;
                orderList.clear();
                if (!uid.equals(FirebaseAuth.getInstance().getUid())) {
                    adapter.notifyDataSetChanged();
                    return;
                }
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Object value = childSnapshot.getValue();
                    if (!(value instanceof Map)) continue;
                    Order order = CustomerBookingFields.read(childSnapshot.getKey(), (Map<?, ?>) value);
                    // 不再读取旧隐藏标记：之前隐藏的订单也正常显示，仍只展示当前用户的数据。
                    if (uid.equals(order.getCustomerId()))
                        orderList.add(order);
                }
                Log.d("FirebaseDebug", "Total orders in list: " + orderList.size());
                getBinding().layoutEmptyState.setVisibility(orderList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isDestroyed()) return;
                orderList.clear();
                adapter.notifyDataSetChanged();
                showMessage("Cannot load your orders.");
            }
        };
        ownOrders.addValueEventListener(ordersListener);
        tablesRef = FirebaseDatabase.getInstance().getReference("tables");
        tablesListener = new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isDestroyed()) return;
                Map<String, String> numbers = new HashMap<>();
                for (DataSnapshot table : snapshot.getChildren())
                    numbers.put(table.getKey(), CustomerBookingFields.text(table.child("tableNumber").getValue()));
                adapter.setTableNumbers(numbers);
            }
            public void onCancelled(@NonNull DatabaseError error) { adapter.setTableNumbers(new HashMap<>()); }
        };
        tablesRef.addValueEventListener(tablesListener);
    }

    private void cancelBooking(String id) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || !CustomerBookingFields.validKey(id) || !cancelling.add(id)) return;
        CustomerBookingCancellation.cancel(id, (error, orderCancelled) -> {
            cancelling.remove(id);
            if (isFinishing() || isDestroyed()) return;
            if (error == null) { showMessage("Booking cancelled."); return; }
            if (!orderCancelled) { showMessage(error); return; }
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Reservation release incomplete").setMessage(error)
                    .setPositiveButton("Retry", (dialog, which) -> cancelBooking(id))
                    .setNegativeButton("Close", null).show();
        });
    }

    private void showMessage(String message) {
        if (!isDestroyed()) Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override protected void onDestroy() {
        if (ownOrders != null && ordersListener != null) ownOrders.removeEventListener(ordersListener);
        if (tablesRef != null && tablesListener != null) tablesRef.removeEventListener(tablesListener);
        super.onDestroy();
    }
}
