package com.groupproject.justeat.feature.customer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 直接 Firebase 取消流程；不需要 Repository，也不改其他订单或餐桌现场状态。 */
final class CustomerBookingCancellation {
    interface Callback { void complete(String error, boolean orderCancelled); }

    static void cancel(String id, Callback callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (!CustomerBookingFields.validKey(id) || !CustomerBookingFields.validKey(uid)) {
            callback.complete("Please sign in again.", false);
            return;
        }
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        // 先预读，避免首次事务在空缓存上直接退出；真正的校验仍在事务每次重试时执行。
        DatabaseReference orderRef = root.child("Orders").child(id);
        orderRef.get().addOnSuccessListener(initial -> {
            if (!(initial.getValue() instanceof Map)) {
                callback.complete("Order is unavailable.", false);
                return;
            }
            orderRef.runTransaction(new Transaction.Handler() {
                private String reason;
                @Override public Transaction.Result doTransaction(MutableData current) {
                    reason = null;
                    if (!uid.equals(FirebaseAuth.getInstance().getUid())) {
                        reason = "Please sign in again.";
                        return Transaction.abort();
                    }
                    // 缓存为空时请求服务器确认；不创建缺失的订单。
                    if (current.getValue() == null) return Transaction.success(current);
                    if (!(current.getValue() instanceof Map)) return Transaction.abort();
                    try {
                        current.setValue(CustomerBookingFields.cancelledOrder(id, uid, (Map<?, ?>) current.getValue()));
                        return Transaction.success(current);
                    } catch (IllegalArgumentException e) {
                        reason = e.getMessage();
                        return Transaction.abort();
                    }
                }
                @Override public void onComplete(DatabaseError error, boolean committed, DataSnapshot current) {
                    if (error != null || !committed || current == null || !current.exists()
                            || !"CANCELLED".equals(current.child("status").getValue())) {
                        callback.complete(reason != null ? reason : "Cancellation failed. Please refresh and retry.", false);
                        return;
                    }
                    String date = CustomerBookingFields.canonicalDate(
                            CustomerBookingFields.text(current.child("bookingDate").getValue()));
                    if (date == null || !uid.equals(current.child("customerId").getValue())) {
                        callback.complete("Cannot verify the cancelled order.", true);
                        return;
                    }
                    releaseDay(root.child("tableReservations").child(date), id, uid, callback);
                }
            }, false);
        }).addOnFailureListener(e -> callback.complete("Cannot load booking. Please retry.", false));
    }

    private static void releaseDay(DatabaseReference day, String id, String uid, Callback callback) {
        if (!uid.equals(FirebaseAuth.getInstance().getUid())) {
            callback.complete("Order cancelled; sign in again to finish releasing its reservation.", true);
            return;
        }
        day.get().addOnSuccessListener(snapshot -> {
            List<DatabaseReference> ownedSlots = new ArrayList<>();
            for (DataSnapshot slot : snapshot.getChildren()) {
                if (CustomerBookingFields.ownsSlot(id, slot.getValue())) ownedSlots.add(slot.getRef());
            }
            releaseNext(ownedSlots, 0, id, uid, callback);
        }).addOnFailureListener(e -> callback.complete(
                "Order cancelled, but its reservation could not be checked. Please retry the release.", true));
    }

    private static void releaseNext(List<DatabaseReference> slots, int index, String id, String uid, Callback callback) {
        if (index == slots.size()) { callback.complete(null, true); return; }
        slots.get(index).runTransaction(new Transaction.Handler() {
            private boolean sessionChanged;
            @Override public Transaction.Result doTransaction(MutableData current) {
                sessionChanged = !uid.equals(FirebaseAuth.getInstance().getUid());
                if (sessionChanged) return Transaction.abort();
                // get() 不保证填充事务缓存。空值也提交校验，服务器有名额时会重试并核对归属。
                if (current.getValue() == null) return Transaction.success(current);
                // 若期间被其他订单占用，则不再删除；事务冲突时会重新检查 owner。
                if (!CustomerBookingFields.ownsSlot(id, current.getValue())) return Transaction.abort();
                current.setValue(null);
                return Transaction.success(current);
            }
            @Override public void onComplete(DatabaseError error, boolean committed, DataSnapshot current) {
                if (error != null || sessionChanged) {
                    callback.complete("Order cancelled, but reservation release failed. Please retry.", true);
                    return;
                }
                releaseNext(slots, index + 1, id, uid, callback);
            }
        }, false);
    }
}
