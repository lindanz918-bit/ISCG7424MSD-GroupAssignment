package com.groupproject.justeat.feature.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView;
import android.view.View;

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
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ActivityCustomerBookTableBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        //setContentView(R.layout.activity_customer_book_table);
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

        String[] guestOptions = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
        ArrayAdapter<String> guestAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                guestOptions
        );
        setDefaultDate();

        guestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getBinding().spinnerGuest.setAdapter(guestAdapter);
        getBinding().spinnerGuest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearTableSelection();
            }
            public void onNothingSelected(AdapterView<?> parent) { clearTableSelection(); }
        });

        getBinding().etDate.setOnClickListener(v -> showDatePickerDialog());

        getBinding().btnSelectTable.setOnClickListener(view -> {
            String date = CustomerBookingFields.canonicalDate(getBinding().etDate.getText().toString());
            if (date == null) return;
            int guests = Integer.parseInt(getBinding().spinnerGuest.getSelectedItem().toString());
            CustomerTableSelectorDialog tableSelectorDialog =
                    CustomerTableSelectorDialog.newInstance(date, guests);
            tableSelectorDialog.setOnTableSelectedListener((tableKey, tableNumber) -> {
                if (!date.equals(CustomerBookingFields.canonicalDate(getBinding().etDate.getText().toString()))
                        || guests != Integer.parseInt(getBinding().spinnerGuest.getSelectedItem().toString())) return;
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
        Calendar calendar = Calendar.getInstance(CustomerBookingFields.RESTAURANT_ZONE);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.UK, "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
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

        selectedDate = CustomerBookingFields.canonicalDate(getBinding().etDate.getText().toString().trim());
        selectedGuestCount = getBinding().spinnerGuest.getSelectedItem() != null
                ? getBinding().spinnerGuest.getSelectedItem().toString() : "1";

        if (selectedDate == null || selectedDate.compareTo(
                CustomerBookingFields.format("yyyy-MM-dd").format(new java.util.Date())) < 0) {
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
        getBinding().spinnerGuest.setEnabled(false);
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
//                if (orderedItems.isEmpty()) {
//                    Toast.makeText(CustomerBookTableActivity.this, "Your saved menu is empty!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                String orderId = ordersRef.push().getKey();
                if (orderId == null) { bookingFailed("Cannot create booking."); return; }
                DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                root.child("tables").child(tableKey).get().addOnSuccessListener(table ->
                    root.child("tableReservations").child(date).child(tableKey).get()
                        .addOnSuccessListener(slot -> {
                            if (isFinishing() || isDestroyed()) return;
                            if (!userId.equals(FirebaseAuth.getInstance().getUid())
                                    || !CustomerBookingFields.available(table.child("isEnabled").getValue(),
                                    table.child("capacity").getValue(), guests, slot.exists())) {
                                bookingFailed("Table is no longer available. Please select again.");
                                return;
                            }
                            // 预检查只改善提示；服务端规则仍必须防止并发占用同一名额。
                            Map<String, Object> updates = CustomerBookingFields.create(
                                    orderId, userId, tableKey, date, guests,
                                    orderedItems.isEmpty() ? null : orderedItems);
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
        Calendar calendar = Calendar.getInstance(CustomerBookingFields.RESTAURANT_ZONE);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String defaultDate = String.format(Locale.UK, "%02d/%02d/%d", day, month, year);
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
        getBinding().spinnerGuest.setEnabled(true);
        getBinding().etDate.setEnabled(true);
        clearTableSelection();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
