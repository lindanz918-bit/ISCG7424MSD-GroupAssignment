package com.groupproject.justeat.feature.staff;

import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

/** 直接 Firebase 的 Staff 写入工具，不是 Repository。
 * 多路径更新保持状态与名额原子一致；并发/权限最终由配套 Security Rules 拦截。
 * 不执行根节点事务，不读取整库，不自动修复旧数据或缺失名额。
 */
public final class StaffBookingUpdates {
    public interface Callback { void complete(String error); }
    private StaffBookingUpdates() {}
    public static void changeStatus(String id, String target, Callback callback) {
        if (!StaffBookingPolicy.validKey(id)) { callback.complete("Invalid order ID."); return; }
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        StaffAccess.check(() -> root.child("Orders").child(id).get().addOnSuccessListener(order -> {
            String current = StaffTableState.text(order, "status");
            boolean allowed = "COMPLETED".equals(target) ? StaffOrderActionPolicy.isActive(current)
                    : StaffOrderActionPolicy.canApply(current, target);
            if (!allowed) { callback.complete("Order changed. Refresh and try again."); return; }
            String date = StaffTableState.text(order, "bookingDate");
            String table = StaffTableState.text(order, "tableId");
            if (!StaffBookingPolicy.validDate(date) || !StaffBookingPolicy.validKey(table)) {
                callback.complete("Booking requires migration: use yyyy-MM-dd and the database table key."); return;
            }
            root.child("tables").child(table).get().addOnSuccessListener(tableData -> {
                if (!tableData.exists()) { callback.complete("Unknown table key. Migrate this booking first."); return; }
                String slot = "tableReservations/" + date + "/" + table;
                root.child(slot).get().addOnSuccessListener(reservation -> {
                    if (!id.equals(StaffTableState.text(reservation, "orderId"))) {
                        callback.complete("Reservation missing or owned by another order. No changes saved."); return;
                    }
                    if ("COMPLETED".equals(target)) {
                        root.child("OrderItems").child(id).get().addOnSuccessListener(items -> {
                            for (DataSnapshot item : items.getChildren()) {
                                if (!Boolean.TRUE.equals(order.child("servedItems").child(item.getKey()).getValue())) {
                                    callback.complete("Serve every meal before completing the order."); return;
                                }
                            }
                            write(root, id, target, slot, callback);
                        }).addOnFailureListener(e -> callback.complete(e.getMessage()));
                    } else write(root, id, target, slot, callback);
                }).addOnFailureListener(e -> callback.complete(e.getMessage()));
            }).addOnFailureListener(e -> callback.complete(e.getMessage()));
        }).addOnFailureListener(e -> callback.complete(e.getMessage())), callback::complete);
    }
    private static void write(DatabaseReference root, String id, String target, String slot, Callback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Orders/" + id + "/status", target);
        updates.put("Orders/" + id + "/updatedAt", ServerValue.TIMESTAMP);
        // COMPLETED keeps the slot. Releasing the physical table is a separate operation.
        if (StaffBookingPolicy.releasesDay(target)) updates.put(slot, null);
        root.updateChildren(updates).addOnSuccessListener(v -> callback.complete(null))
                .addOnFailureListener(e -> callback.complete("Nothing saved. Booking changed or Firebase rules denied the update."));
    }
}
