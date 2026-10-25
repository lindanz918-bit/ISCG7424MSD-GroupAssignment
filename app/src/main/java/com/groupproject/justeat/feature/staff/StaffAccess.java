package com.groupproject.justeat.feature.staff;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.function.Consumer;

/**
 * Checks the current employee before a read or write. Server rules must enforce the same
 * permission.
 */
final class StaffAccess {
    static void check(Runnable allowed, Consumer<String> denied) {
        try {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                denied.accept("Please sign in with an active Staff account.");
                return;
            }
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .get()
                    .addOnSuccessListener(
                            s -> {
                                if (!uid.equals(FirebaseAuth.getInstance().getUid())
                                        || !"STAFF".equals(s.child("role").getValue(String.class))
                                        || !"ACTIVE"
                                                .equals(s.child("status").getValue(String.class))) {
                                    denied.accept("An active Staff account is required.");
                                    return;
                                }
                                allowed.run();
                            })
                    .addOnFailureListener(
                            e ->
                                    denied.accept(
                                            "Cannot verify Staff access. Check your connection."));
        } catch (RuntimeException e) {
            denied.accept("Firebase is unavailable. Please sign in again.");
        }
    }
}
