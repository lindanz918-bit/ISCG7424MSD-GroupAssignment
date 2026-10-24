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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ServerValue;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Locale;

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
                        .setTitle("Delete Order")
                        .setMessage("Hide this order from My Orders on this device? This does not cancel the order or release its table reservation.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            String uid = FirebaseAuth.getInstance().getUid();
                            if (uid == null || !uid.equals(order.getCustomerId())) return;
                            // 只保存本机当前账号的隐藏标记，不修改 Firebase 订单或预约。
                            getSharedPreferences("customer_deleted_orders", MODE_PRIVATE).edit()
                                    .putBoolean(uid + ":" + order.getOrderId(), true).apply();
                            orderList.remove(order);
                            adapter.notifyDataSetChanged();
                            getBinding().layoutEmptyState.setVisibility(orderList.isEmpty()
                                    ? android.view.View.VISIBLE : android.view.View.GONE);
                        })
                        .setNegativeButton("Back", null)
                        .show();
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
                    Order order = new Order();
                    order.setOrderId(childSnapshot.getKey());
                    order.setCustomerId(childSnapshot.child("customerId").getValue(String.class));
                    order.setBookingDate(childSnapshot.child("bookingDate").getValue(String.class));
                    order.setTableId(childSnapshot.child("tableId").getValue(String.class));
                    order.setStatus(childSnapshot.child("status").getValue(String.class));
                    Object guests = childSnapshot.child("guestCount").getValue();
                    order.setGuestCount(guests instanceof Number ? guests.toString() : "");
                    Object createdAt = childSnapshot.child("createdAt").getValue();
                    order.setCreatedAt(createdAt instanceof Number ? ((Number) createdAt).longValue() : 0L);
                    // 仅过滤本次 Delete 的本地标记，之前已恢复的旧隐藏订单不受影响。
                    if (uid.equals(order.getCustomerId()) && !getSharedPreferences(
                            "customer_deleted_orders", MODE_PRIVATE).getBoolean(uid + ":" + order.getOrderId(), false))
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
                    numbers.put(table.getKey(), table.child("tableNumber").getValue(String.class));
                adapter.setTableNumbers(numbers);
            }
            public void onCancelled(@NonNull DatabaseError error) { adapter.setTableNumbers(new HashMap<>()); }
        };
        tablesRef.addValueEventListener(tablesListener);
    }

    private void cancelBooking(String id) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null || !validKey(id) || !cancelling.add(id)) return;
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        // 直接在页面中操作 Firebase，事务会在数据发生变化时重新检查。
        root.child("Orders").child(id).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData order) {
                if (!uid.equals(FirebaseAuth.getInstance().getUid())) return Transaction.abort();
                // 首次可能只有空缓存，让服务器确认，不创建不存在的订单。
                if (order.getValue() == null) return Transaction.success(order);
                if (!(order.getValue() instanceof Map)) return Transaction.abort();
                Object statusValue = order.child("status").getValue();
                Object dateValue = order.child("bookingDate").getValue();
                Object tableValue = order.child("tableId").getValue();
                if (!(statusValue instanceof String) || !(dateValue instanceof String)
                        || !(tableValue instanceof String)) return Transaction.abort();
                String status = (String) statusValue;
                String date = (String) dateValue;
                String tableId = (String) tableValue;
                boolean canCancel = "PENDING".equals(status) || "ACCEPTED".equals(status)
                        || "PREPARING".equals(status) || "READY".equals(status);
                if (!uid.equals(order.child("customerId").getValue())
                        || (!canCancel && !"CANCELLED".equals(status))
                        || !validKey(tableId) || !validBookingDate(date)) return Transaction.abort();
                if (!"CANCELLED".equals(status)) {
                    order.child("status").setValue("CANCELLED");
                    order.child("updatedAt").setValue(ServerValue.TIMESTAMP);
                }
                return Transaction.success(order);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot order) {
                if (error != null || !committed || order == null || !order.exists()) {
                    finishCancellation(id, "Cannot cancel this order. Please refresh and try again.", false);
                    return;
                }
                String date = order.child("bookingDate").getValue(String.class);
                String tableId = order.child("tableId").getValue(String.class);
                DatabaseReference reservation = root.child("tableReservations").child(date).child(tableId);
                releaseReservation(id, uid, reservation);
            }
        }, false);
    }

    private void releaseReservation(String id, String uid, DatabaseReference reservation) {
        reservation.runTransaction(new Transaction.Handler() {
            private boolean signedOut;

            @Override
            public Transaction.Result doTransaction(MutableData slot) {
                signedOut = !uid.equals(FirebaseAuth.getInstance().getUid());
                if (signedOut) return Transaction.abort();
                if (slot.getValue() == null) return Transaction.success(slot);
                // 只释放本订单的预约，别人重新占用的名额不能删除。
                if (!id.equals(slot.child("orderId").getValue())) return Transaction.abort();
                slot.setValue(null);
                return Transaction.success(slot);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot slot) {
                String message = error != null || signedOut
                        ? "Order cancelled, but reservation release failed. Please retry." : null;
                finishCancellation(id, message, true);
            }
        }, false);
    }

    private void finishCancellation(String id, String error, boolean orderCancelled) {
        cancelling.remove(id);
        if (isFinishing() || isDestroyed()) return;
        if (error == null || !orderCancelled) {
            showMessage(error == null ? "Booking cancelled." : error);
            return;
        }
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Reservation release incomplete")
                .setMessage(error)
                .setPositiveButton("Retry", (dialog, which) -> cancelBooking(id))
                .setNegativeButton("Close", null)
                .show();
    }

    private boolean validKey(String key) {
        if (key == null || key.isEmpty()) return false;
        for (char c : key.toCharArray()) {
            if (".#$[]/".indexOf(c) >= 0 || Character.isISOControl(c)) return false;
        }
        return true;
    }

    private boolean validBookingDate(String date) {
        if (date == null || !date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(date, position) != null && position.getIndex() == date.length();
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
