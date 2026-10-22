package com.groupproject.justeat.feature.staff;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.database.*;
import com.groupproject.justeat.base.BaseViewModel;
import com.groupproject.justeat.data.model.Order;
import java.util.*;
/** Live orders from the same Orders node used by Customer. */
public class StaffOrderViewModel extends BaseViewModel {
    private final MutableLiveData<List<Order>> orders = new MutableLiveData<>(Collections.emptyList());
    private DatabaseReference reference;
    private ValueEventListener listener;
    private boolean closed;
    public LiveData<List<Order>> getOrders() { return orders; }
    public void loadPendingOrders() {
        if (listener != null) return;
        setLoading(true);
        StaffAccess.check(() -> {
            if (closed) return;
            reference = FirebaseDatabase.getInstance().getReference("Orders");
            listener = new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    List<Order> data = new ArrayList<>();
                    try {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Order order = child.getValue(Order.class);
                            if (order != null) { order.setOrderId(child.getKey()); data.add(order); }
                        }
                        data.sort((a,b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                        orders.setValue(data);
                    } catch (RuntimeException e) { setErrorMessage("Some order data could not be read."); }
                    setLoading(false);
                }
                public void onCancelled(DatabaseError error) { fail(error.getMessage()); }
            };
            reference.addValueEventListener(listener);
        }, this::fail);
    }
    public void applyStatus(Order order, String target) {
        if (order == null || Boolean.TRUE.equals(getLoading().getValue())) return;
        setLoading(true);
        StaffAccess.check(() -> FirebaseDatabase.getInstance().getReference("Orders")
            .child(order.getOrderId()).child("status").runTransaction(new Transaction.Handler() {
                public Transaction.Result doTransaction(MutableData data) {
                    String current = data.getValue(String.class);
                    if (current == null) return Transaction.success(data);
                    if (!StaffOrderActionPolicy.canApply(current, target)) return Transaction.abort();
                    data.setValue(target);
                    return Transaction.success(data);
                }
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                    setLoading(false);
                    if (error != null || !committed || snapshot == null || !target.equals(snapshot.getValue(String.class)))
                        setErrorMessage("Order changed or update failed. Refresh and try again.");
                }
            }), this::fail);
    }
    private void fail(String message) { setLoading(false); setErrorMessage(message); }
    public void consumeError() { setErrorMessage(null); }
    protected void onCleared() {
        closed = true;
        if (reference != null && listener != null) reference.removeEventListener(listener);
    }
}
