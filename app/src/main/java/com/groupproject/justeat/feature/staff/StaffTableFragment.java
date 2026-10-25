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
import com.groupproject.justeat.data.model.RestaurantTable;
import com.groupproject.justeat.databinding.ActivityStaffTablesBinding;
import com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity;

import java.util.*;

/** Restaurant Staff 餐桌可用/不可用管理页面。 */
public class StaffTableFragment extends Fragment {
    private AlertDialog activeDialog;
    private DatabaseReference tablesRef, ordersRef;
    private ValueEventListener tablesListener, ordersListener;
    private DataSnapshot bookings;
    private boolean busy;
    private final Map<String, String> names = new HashMap<>();
    private final Set<String> requestedNames = new HashSet<>();
    private StaffTableAdapter adapter;

    private ActivityStaffTablesBinding binding;

    private ActivityStaffTablesBinding getBinding() {
        return binding;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = ActivityStaffTablesBinding.inflate(inflater, container, false);
        binding.getRoot().setFitsSystemWindows(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
        super.onViewCreated(view, savedState);

        adapter =
                new StaffTableAdapter(
                        (table, occupancy) -> {
                            if (!occupancy)
                                activeDialog =
                                        new AlertDialog.Builder(requireContext())
                                                .setMessage(
                                                        Boolean.TRUE.equals(table.getIsEnabled())
                                                                ? "Disable new bookings? Existing"
                                                                      + " reservations are kept."
                                                                : "Enable new bookings for this"
                                                                      + " table?")
                                                .setNegativeButton("Cancel", null)
                                                .setPositiveButton(
                                                        "Confirm",
                                                        (dialog, which) ->
                                                                updateTable(table, false))
                                                .show();
                            else
                                activeDialog =
                                        new AlertDialog.Builder(requireContext())
                                                .setMessage(
                                                        "AVAILABLE"
                                                                        .equalsIgnoreCase(
                                                                                table
                                                                                        .getOccupancyStatus())
                                                                ? "Mark this table occupied?"
                                                                : "Release this table for new"
                                                                      + " guests? Existing orders"
                                                                      + " will not be changed.")
                                                .setNegativeButton("Cancel", null)
                                                .setPositiveButton(
                                                        "Confirm",
                                                        (dialog, which) -> updateTable(table, true))
                                                .show();
                        });
        getBinding().rvStaffTables.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvStaffTables.setAdapter(adapter);
        getBinding()
                .btnStaffTablesBack
                .setVisibility(
                        getActivity() instanceof StaffDashBoardActivity ? View.GONE : View.VISIBLE);
        getBinding().btnStaffTablesBack.setOnClickListener(v -> returnHome());
        loadTables();
    }

    private void loadTables() {
        setBusy(true);
        StaffAccess.check(
                () -> {
                    if (binding == null) return;
                    tablesRef = FirebaseDatabase.getInstance().getReference("tables");
                    tablesListener =
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (binding == null) return;
                                    List<RestaurantTable> tables = new ArrayList<>();
                                    int enabled = 0, disabled = 0, occupied = 0, migration = 0;
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        RestaurantTable table =
                                                child.getValue(RestaurantTable.class);
                                        if (table == null) continue;
                                        table.setTableId(child.getKey());
                                        tables.add(table);
                                        if (!table.hasValidState()) {
                                            migration++;
                                            continue;
                                        }
                                        if (table.getIsEnabled()) enabled++;
                                        else disabled++;
                                        if ("OCCUPIED".equals(table.getOccupancyStatus()))
                                            occupied++;
                                    }
                                    adapter.submitList(tables);
                                    binding.tvStaffTableSummary.setText(
                                            "Bookings enabled: "
                                                    + enabled
                                                    + "   Disabled: "
                                                    + disabled
                                                    + "\nOccupied now: "
                                                    + occupied
                                                    + "   Migration needed: "
                                                    + migration
                                                    + "\n"
                                                    + "On-site state does not change date"
                                                    + " reservations.");
                                    binding.layoutStaffTablesEmpty.setVisibility(
                                            tables.isEmpty() ? View.VISIBLE : View.GONE);
                                    binding.progressStaffTables.setVisibility(View.GONE);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    showError(error.getMessage());
                                }
                            };
                    ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
                    ordersListener =
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    bookings = snapshot;
                                    showBookings();
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    showError(error.getMessage());
                                }
                            };
                    setBusy(false);
                    tablesRef.addValueEventListener(tablesListener);
                    ordersRef.addValueEventListener(ordersListener);
                },
                this::showError);
    }

    private void showBookings() {
        if (binding == null || bookings == null) return;
        Map<String, String> labels = new HashMap<>();
        for (DataSnapshot order : bookings.getChildren()) {
            String status = order.child("status").getValue(String.class);
            if (!("PENDING".equals(status) || Order.isActive(status) || "COMPLETED".equals(status)))
                continue;
            String uid = order.child("customerId").getValue(String.class);
            String table = order.child("tableId").getValue(String.class);
            String label =
                    names.getOrDefault(uid, uid)
                            + " • "
                            + order.child("bookingDate").getValue()
                            + " • "
                            + order.child("guestCount").getValue()
                            + " guests • "
                            + status;
            labels.put(table, labels.containsKey(table) ? labels.get(table) + "\n" + label : label);
            if (uid != null && requestedNames.add(uid)) {
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .get()
                        .addOnSuccessListener(
                                user -> {
                                    if (binding == null) return;
                                    String first = user.child("firstName").getValue(String.class);
                                    String last = user.child("lastName").getValue(String.class);
                                    String name =
                                            ((first == null ? "" : first)
                                                            + " "
                                                            + (last == null ? "" : last))
                                                    .trim();
                                    if (!name.isEmpty()) names.put(uid, name);
                                    showBookings();
                                });
            }
        }
        adapter.setBookings(labels);
    }

    private void updateTable(RestaurantTable table, boolean occupancy) {
        if (busy || !table.hasValidState()) return;
        String field = occupancy ? "occupancyStatus" : "isEnabled";
        Object expected = occupancy ? table.getOccupancyStatus() : table.getIsEnabled();
        Object target =
                occupancy
                        ? ("AVAILABLE".equals(table.getOccupancyStatus())
                                ? "OCCUPIED"
                                : "AVAILABLE")
                        : !table.getIsEnabled();
        setBusy(true);
        StaffAccess.check(
                () ->
                        tablesRef
                                .child(table.getTableId())
                                .runTransaction(
                                        new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(
                                                    MutableData data) {
                                                if (data.getValue() == null)
                                                    return Transaction.success(data);
                                                if (!expected.equals(data.child(field).getValue()))
                                                    return Transaction.abort();
                                                data.child(field).setValue(target);
                                                data.child("updatedAt")
                                                        .setValue(ServerValue.TIMESTAMP);
                                                return Transaction.success(data);
                                            }

                                            @Override
                                            public void onComplete(
                                                    DatabaseError error,
                                                    boolean committed,
                                                    DataSnapshot data) {
                                                setBusy(false);
                                                if (error != null
                                                        || !committed
                                                        || data == null
                                                        || !target.equals(
                                                                data.child(field).getValue()))
                                                    showError(
                                                            "Table changed or update denied. Please"
                                                                + " refresh.");
                                            }
                                        },
                                        false),
                this::showError);
    }

    private void setBusy(boolean loading) {
        busy = loading;
        if (binding == null) return;
        adapter.setBusy(loading);
        binding.progressStaffTables.setVisibility(loading ? View.VISIBLE : View.GONE);
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
        if (tablesRef != null && tablesListener != null)
            tablesRef.removeEventListener(tablesListener);
        if (ordersRef != null && ordersListener != null)
            ordersRef.removeEventListener(ordersListener);
        bookings = null;
        requestedNames.clear();
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
        binding.rvStaffTables.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }
}
