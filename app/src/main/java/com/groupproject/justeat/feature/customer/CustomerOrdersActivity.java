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
            public void onDeleteClick(Order order, int position) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(CustomerOrdersActivity.this)
                        .setTitle("Hide Order")
                        .setMessage("Hide this order on this device? The booking record will be kept.")
                        .setPositiveButton("Hide", (dialog, which) -> {
                            String uid = FirebaseAuth.getInstance().getUid();
                            if (uid == null) return;
                            getSharedPreferences("hidden_customer_orders", MODE_PRIVATE).edit()
                                    .putBoolean(uid + ":" + order.getOrderId(), true).apply();
                            orderList.remove(order);
                            adapter.notifyDataSetChanged();
                            getBinding().layoutEmptyState.setVisibility(orderList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
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
                    if (uid.equals(order.getCustomerId()) && !getSharedPreferences(
                            "hidden_customer_orders", MODE_PRIVATE).getBoolean(uid + ":" + order.getOrderId(), false))
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
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        ordersRef.child(id).get().addOnSuccessListener(snapshot -> {
            Object value = snapshot.getValue();
            if (!(value instanceof Map)) { cancelFailed(id, "Order is unavailable."); return; }
            Map<?, ?> order = (Map<?, ?>) value;
            String date = CustomerBookingFields.text(order.get("bookingDate"));
            String table = CustomerBookingFields.text(order.get("tableId"));
            if (!uid.equals(order.get("customerId")) || !"PENDING".equals(order.get("status"))
                    || !date.equals(CustomerBookingFields.canonicalDate(date))
                    || !CustomerBookingFields.validKey(table)) {
                cancelFailed(id, "Only pending bookings using the new format can be cancelled. Please contact staff.");
                return;
            }
            root.child("tableReservations").child(date).child(table).get().addOnSuccessListener(slot -> {
                try {
                    if (!uid.equals(FirebaseAuth.getInstance().getUid())) {
                        cancelFailed(id, "Please sign in again."); return;
                    }
                    Map<String, Object> updates = CustomerBookingFields.cancel(id, uid, order,
                            CustomerBookingFields.text(slot.child("orderId").getValue()));
                    // 规则必须在提交时再次验证状态及名额归属，不能只依赖上面的预读取。
                    root.updateChildren(updates).addOnSuccessListener(unused -> {
                        cancelling.remove(id);
                        showMessage("Booking cancelled.");
                    }).addOnFailureListener(e -> cancelFailed(id, "Cancellation failed. Please refresh."));
                } catch (IllegalArgumentException e) { cancelFailed(id, e.getMessage()); }
            }).addOnFailureListener(e -> cancelFailed(id, "Cannot verify reservation."));
        }).addOnFailureListener(e -> cancelFailed(id, "Cannot load booking."));
    }

    private void cancelFailed(String id, String message) {
        cancelling.remove(id);
        showMessage(message);
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
