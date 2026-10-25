package com.groupproject.justeat.feature.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.databinding.ActivityCustomerBookTableBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import com.google.firebase.database.ServerValue;
import com.groupproject.justeat.data.model.TableReservation;

public class CustomerBookTableActivity extends BaseActivity<ActivityCustomerBookTableBinding> {
    String selectedGuestCount, selectedDate, selectedTableId;
    private boolean bookingInFlight;


    @Override
    protected ActivityCustomerBookTableBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCustomerBookTableBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();
        initData();
    }

    private void initView() {
        getBinding().btnBack.setOnClickListener(v -> {
            finish();
        });

        getBinding().numberPickerGuests.setMinValue(1);
        getBinding().numberPickerGuests.setMaxValue(20);
        getBinding().numberPickerGuests.setValue(1);

        setDefaultDate();

        getBinding().etDate.setOnClickListener(v -> showDatePickerDialog());

        getBinding().btnSelectTable.setOnClickListener(view -> {
            String date = selectedDate;
            if (date == null) return;
            int guests = getBinding().numberPickerGuests.getValue();
            CustomerTableSelectorDialog tableSelectorDialog =
                    CustomerTableSelectorDialog.newInstance(date, guests);
            tableSelectorDialog.setOnTableSelectedListener((tableKey, tableNumber) -> {
                if (!date.equals(selectedDate)
                        || guests != Integer.parseInt(String.valueOf(getBinding().numberPickerGuests.getValue()))) return;
                getBinding().btnSelectTable.setText(tableNumber);
                selectedTableId = tableKey;
            });
            tableSelectorDialog.show(getSupportFragmentManager(), "tableSelectorDialog");
        });

        getBinding().bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, CustomerDashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_my_menu) {
                startActivity(new Intent(this, CustomerMyMenuActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, CustomerOrdersActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(this, CustomerAccountActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void initData() {
        getBinding().btnBookConfirm.setOnClickListener(v -> {
            confirmBooking();
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Pacific/Auckland"));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.UK, "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    selectedDate = String.format(Locale.UK, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    getBinding().etDate.setText(formattedDate);
                    clearTableSelection();
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void confirmBooking() {
        if (bookingInFlight) return;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        selectedGuestCount = String.valueOf(getBinding().numberPickerGuests.getValue());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
        if (selectedDate == null || selectedDate.compareTo(dateFormat.format(new java.util.Date())) < 0) {
            Toast.makeText(this, "Please select a booking date!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTableId == null || selectedTableId.isEmpty()) {
            Toast.makeText(this, "Please select a table!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        final String date = selectedDate;
        final String tableKey = selectedTableId;
        final int guests = Integer.parseInt(selectedGuestCount);
        bookingInFlight = true;
        getBinding().btnBookConfirm.setEnabled(false);
        getBinding().btnSelectTable.setEnabled(false);
        getBinding().numberPickerGuests.setEnabled(false);
        getBinding().etDate.setEnabled(false);
        DatabaseReference myMenuRef = FirebaseDatabase.getInstance().getReference("MyMenu").child(userId);
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        myMenuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                List<MenuItem> orderedItems = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    MenuItem item = itemSnapshot.getValue(MenuItem.class);
                    if (item != null) {
                        orderedItems.add(item);
                    }
                }
                String orderId = ordersRef.push().getKey();
                if (orderId == null) { bookingFailed("Cannot create booking."); return; }
                DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                //Check whether table is available with database table "tables"
                //Check whether the date is available with database table "tableReservations".
                root.child("tables").child(tableKey).get().addOnSuccessListener(table ->
                    root.child("tableReservations").child(date).child(tableKey).get()
                        .addOnSuccessListener(slot -> {
                            if (isFinishing() || isDestroyed()) return;
                            Object capacity = table.child("capacity").getValue();
                            //check whether the capacity is large enough
                            if (!userId.equals(FirebaseAuth.getInstance().getUid())
                                    || !Boolean.TRUE.equals(table.child("isEnabled").getValue())
                                    || !(capacity instanceof Number) || ((Number) capacity).intValue() < guests
                                    || guests <= 0 || slot.exists()) {
                                bookingFailed("Table is no longer available. Please select again.");
                                return;
                            }
                            Map<String, Object> order = new HashMap<>();
                            order.put("orderId", orderId);
                            order.put("customerId", userId);
                            order.put("tableId", tableKey);
                            order.put("bookingDate", date);
                            order.put("guestCount", guests);
                            order.put("status", "PENDING");
                            order.put("createdAt", ServerValue.TIMESTAMP);
                            order.put("updatedAt", ServerValue.TIMESTAMP);
                            Map<String, Object> updates = new HashMap<>();
                            //update database table "Orders" and "tableReservations" and "OrderItems" when has items
                            updates.put("Orders/" + orderId, order);
                            updates.put("tableReservations/" + date + "/" + tableKey, new TableReservation(orderId));
                            if (!orderedItems.isEmpty()) updates.put("OrderItems/" + orderId, orderedItems);
                            root.updateChildren(updates).addOnSuccessListener(unused -> {
                                if (isFinishing() || isDestroyed()) return;
                                Toast.makeText(CustomerBookTableActivity.this,
                                        "Booking Successful!", Toast.LENGTH_SHORT).show();
                                myMenuRef.removeValue();
                                startActivity(new Intent(CustomerBookTableActivity.this, CustomerOrdersActivity.class));
                                finish();
                            }).addOnFailureListener(e -> bookingFailed("Booking failed. Please refresh and retry."));
                        }).addOnFailureListener(e -> bookingFailed("Cannot check reservation availability."))
                ).addOnFailureListener(e -> bookingFailed("Cannot check table availability."));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bookingFailed("Error fetching menu data");
            }
        });
    }
    private void setDefaultDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Pacific/Auckland"));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String defaultDate = String.format(Locale.UK, "%02d/%02d/%d", day, month, year);
        selectedDate = String.format(Locale.UK, "%04d-%02d-%02d", year, month, day);
        getBinding().etDate.setText(defaultDate);
    }

    private void clearTableSelection() {
        selectedTableId = null;
        getBinding().btnSelectTable.setText("Select a Table");
    }

    private void bookingFailed(String message) {
        bookingInFlight = false;
        if (isDestroyed()) return;
        getBinding().btnBookConfirm.setEnabled(true);
        getBinding().btnSelectTable.setEnabled(true);
        getBinding().numberPickerGuests.setEnabled(true);
        getBinding().etDate.setEnabled(true);
        clearTableSelection();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
