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
                            // Staff-only parsing accepts both legacy string and new numeric guestCount.
                            Order order = new Order(child.getKey(), StaffTableState.text(child,"customerId"),
                                StaffTableState.text(child,"guestCount"), StaffTableState.text(child,"bookingDate"),
                                StaffTableState.text(child,"tableId"), StaffTableState.text(child,"status"),
                                child.child("createdAt").getValue() instanceof Number
                                    ? ((Number)child.child("createdAt").getValue()).longValue() : 0L);
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
        StaffBookingUpdates.changeStatus(order.getOrderId(), target, error -> {
            setLoading(false);
            if (error != null) setErrorMessage(error);
        });
    }

    private void fail(String message) { setLoading(false); setErrorMessage(message); }
    public void consumeError() { setErrorMessage(null); }
    protected void onCleared() {
        closed = true;
        if (reference != null && listener != null) reference.removeEventListener(listener);
    }
}
