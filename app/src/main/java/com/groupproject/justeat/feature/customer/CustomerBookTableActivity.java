package com.groupproject.justeat.feature.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.databinding.ActivityCustomerBookTableBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CustomerBookTableActivity extends BaseActivity<ActivityCustomerBookTableBinding> {
    String selectedGuestCount, selectedDate, selectedTableId;


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

        getBinding().etDate.setOnClickListener(v -> showDatePickerDialog());

        getBinding().btnSelectTable.setOnClickListener(view -> {
            CustomerTableSelectorDialog tableSelectorDialog = new CustomerTableSelectorDialog();
            tableSelectorDialog.setOnTableSelectedListener(tableNumber -> {
                getBinding().btnSelectTable.setText(tableNumber);
                selectedTableId = tableNumber;
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
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    getBinding().etDate.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void confirmBooking() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        selectedDate = getBinding().etDate.getText().toString().trim();
        selectedGuestCount = getBinding().spinnerGuest.getSelectedItem() != null
                ? getBinding().spinnerGuest.getSelectedItem().toString() : "1";

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a booking date!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTableId == null || selectedTableId.isEmpty()) {
            Toast.makeText(this, "Please select a table!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("BookingDebug", "selectedDate: " + selectedDate + ", tableId: " + selectedTableId);

        String userId = currentUser.getUid();
        DatabaseReference myMenuRef = FirebaseDatabase.getInstance().getReference("MyMenu").child(userId);
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        DatabaseReference orderItemRef = FirebaseDatabase.getInstance().getReference("OrderItems");

        myMenuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                if (orderId == null) return;

                selectedGuestCount = getBinding().spinnerGuest.getSelectedItem() != null
                        ? getBinding().spinnerGuest.getSelectedItem().toString() : "1";
                selectedDate = getBinding().etDate.getText().toString();
                Log.d("BookingDebug", "selectedDate: " + selectedDate);

                Order bookingOrder = new Order(
                        orderId,
                        userId,
                        selectedGuestCount,
                        selectedDate,
                        selectedTableId,
                        "PENDING",
                        System.currentTimeMillis()
                );

                if (orderId != null) {
                    ordersRef.child(orderId).setValue(bookingOrder)
                            .addOnSuccessListener(aVoid -> {
                                orderItemRef.child(orderId).setValue(orderedItems)
                                        .addOnSuccessListener(Unsed -> {
                                            Toast.makeText(CustomerBookTableActivity.this, "Booking Successful!", Toast.LENGTH_SHORT).show();
                                            myMenuRef.removeValue();
                                            Intent intent = new Intent(CustomerBookTableActivity.this, CustomerDashboardActivity.class);
                                            startActivity(intent);
                                            finish();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(CustomerBookTableActivity.this, "Failed to book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerBookTableActivity.this, "Error fetching menu data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String defaultDate = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year);
        getBinding().etDate.setText(defaultDate);
    }
}