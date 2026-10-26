package com.groupproject.justeat.feature.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.*;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ActivityStaffOrdersBinding;
import com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;
import java.util.TimeZone;

/** Restaurant Staff 订单确认、拒绝和取消页面。 */
public class StaffOrderFragment extends Fragment {
    private AlertDialog activeDialog;
    private DatabaseReference ordersRef;
    private ValueEventListener ordersListener;
    private boolean busy;
    private StaffOrderAdapter adapter;

    private ActivityStaffOrdersBinding binding;

    private ActivityStaffOrdersBinding getBinding() {
        return binding;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = ActivityStaffOrdersBinding.inflate(inflater, container, false);
        binding.getRoot().setFitsSystemWindows(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
        super.onViewCreated(view, savedState);

        adapter =
                new StaffOrderAdapter(
                        (order, target) -> {
                            if ("ACCEPTED".equals(target)) changeStatus(order.getOrderId(), target);
                            else
                                activeDialog =
                                        new AlertDialog.Builder(requireContext())
                                                .setMessage(
                                                        "REJECTED".equals(target)
                                                                ? "Reject this order?"
                                                                : "Cancel this order?")
                                                .setNegativeButton("Keep order", null)
                                                .setPositiveButton(
                                                        "Confirm",
                                                        (dialog, which) ->
                                                                changeStatus(
                                                                        order.getOrderId(), target))
                                                .show();
                        });
        getBinding().rvStaffOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvStaffOrders.setAdapter(adapter);
        getBinding()
                .btnStaffOrdersBack
                .setVisibility(
                        getActivity() instanceof StaffDashBoardActivity ? View.GONE : View.VISIBLE);
        getBinding().btnStaffOrdersBack.setOnClickListener(v -> returnHome());
        loadOrders();
    }

    private void loadOrders() {
        setBusy(true);
        StaffAccess.check(
                () -> {
                    if (binding == null) return;
                    ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
                    ordersListener =
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (binding == null) return;
                                    List<Order> orders = new ArrayList<>();
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        Order order = new Order();
                                        order.setOrderId(child.getKey());
                                        order.setCustomerId(
                                                child.child("customerId").getValue(String.class));
                                        order.setTableId(
                                                child.child("tableId").getValue(String.class));
                                        order.setBookingDate(
                                                child.child("bookingDate").getValue(String.class));
                                        order.setStatus(
                                                child.child("status").getValue(String.class));

                                        Boolean isDeleted = child.child("deletedByCustomer").getValue(Boolean.class);
                                        order.setDeletedByCustomer(isDeleted != null && isDeleted);

                                        Object guests = child.child("guestCount").getValue();
                                        order.setGuestCount(
                                                guests instanceof Number ? guests.toString() : "");
                                        Object created = child.child("createdAt").getValue();
                                        order.setCreatedAt(
                                                created instanceof Number
                                                        ? ((Number) created).longValue()
                                                        : 0L);
                                        orders.add(order);
                                    }
                                    orders.sort(
                                            (a, b) ->
                                                    Long.compare(
                                                            b.getCreatedAt(), a.getCreatedAt()));
                                    adapter.submitList(orders);
                                    int pending = 0, active = 0;
                                    for (Order order : orders) {
                                        if ("PENDING".equals(order.getStatus())) pending++;
                                        if (Order.isActive(order.getStatus())) active++;
                                    }
                                    binding.tvStaffPendingCount.setText(
                                            getString(R.string.staff_ui_pending_count, pending));
                                    binding.tvStaffActiveCount.setText(
                                            getString(R.string.staff_ui_active_count, active));
                                    binding.layoutStaffOrdersEmpty.setVisibility(
                                            orders.isEmpty() ? View.VISIBLE : View.GONE);
                                    // Live updates must not unlock buttons while a write is in
                                    // progress.
                                    binding.progressStaffOrders.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    showError(error.getMessage());
                                }
                            };
                    setBusy(false);
                    ordersRef.addValueEventListener(ordersListener);
                },
                this::showError);
    }

    private void changeStatus(String id, String target) {
        if (busy || !validKey(id)) return;
        setBusy(true);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        StaffAccess.check(
                () ->
                        root.child("Orders")
                                .child(id)
                                .get()
                                .addOnSuccessListener(
                                        order -> {
                                            String current =
                                                    order.child("status").getValue(String.class);
                                            boolean allowed = Order.canStaffChange(current, target);
                                            if (!allowed) {
                                                showError("Order changed. Refresh and try again.");
                                                return;
                                            }
                                            String date =
                                                    order.child("bookingDate")
                                                            .getValue(String.class);
                                            String table =
                                                    order.child("tableId").getValue(String.class);
                                            if (!validDate(date) || !validKey(table)) {
                                                showError(
                                                        "Booking requires migration: use yyyy-MM-dd"
                                                            + " and the database table key.");
                                                return;
                                            }
                                            root.child("tables")
                                                    .child(table)
                                                    .get()
                                                    .addOnSuccessListener(
                                                            tableData -> {
                                                                if (!tableData.exists()) {
                                                                    showError(
                                                                            "Unknown table key."
                                                                                + " Migrate this"
                                                                                + " booking"
                                                                                + " first.");
                                                                    return;
                                                                }
                                                                String slot =
                                                                        "tableReservations/"
                                                                                + date
                                                                                + "/"
                                                                                + table;
                                                                root.child(slot)
                                                                        .get()
                                                                        .addOnSuccessListener(
                                                                                reservation -> {
                                                                                    if (!id.equals(
                                                                                            reservation
                                                                                                    .child(
                                                                                                            "orderId")
                                                                                                    .getValue(
                                                                                                            String
                                                                                                                    .class))) {
                                                                                        showError(
                                                                                                "Reservation"
                                                                                                    + " missing"
                                                                                                    + " or owned"
                                                                                                    + " by another"
                                                                                                    + " order."
                                                                                                    + " No changes"
                                                                                                    + " saved.");
                                                                                        return;
                                                                                    }
                                                                                    writeStatus(
                                                                                            root,
                                                                                            id,
                                                                                            target,
                                                                                            slot);
                                                                                })
                                                                        .addOnFailureListener(
                                                                                e ->
                                                                                        showError(
                                                                                                e
                                                                                                        .getMessage()));
                                                            })
                                                    .addOnFailureListener(
                                                            e -> showError(e.getMessage()));
                                        })
                                .addOnFailureListener(e -> showError(e.getMessage())),
                this::showError);
    }

    private void writeStatus(DatabaseReference root, String id, String target, String slot) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("Orders/" + id + "/status", target);
        updates.put("Orders/" + id + "/updatedAt", ServerValue.TIMESTAMP);
        if ("REJECTED".equals(target) || "CANCELLED".equals(target)) updates.put(slot, null);
        root.updateChildren(updates)
                .addOnSuccessListener(v -> setBusy(false))
                .addOnFailureListener(e -> showError("Nothing saved. Please refresh and retry."));
    }

    private void setBusy(boolean loading) {
        busy = loading;
        if (binding == null) return;
        adapter.setBusy(loading);
        binding.progressStaffOrders.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        setBusy(false);
        if (binding != null) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void returnHome() {
        if (getActivity() instanceof StaffDashBoardActivity)
            ((StaffDashBoardActivity) requireActivity()).selectTab(R.id.staff_nav_home);
        else requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        if (ordersRef != null && ordersListener != null)
            ordersRef.removeEventListener(ordersListener);
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
        binding.rvStaffOrders.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }

    private static boolean validKey(String key) {
        if (key == null || key.isEmpty()) return false;
        for (char c : key.toCharArray()) {
            if (".#$[]/".indexOf(c) >= 0 || Character.isISOControl(c)) return false;
        }
        return true;
    }

    private static boolean validDate(String date) {
        if (date == null || !date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        format.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(date, position) != null && position.getIndex() == date.length();
    }
}
