package com.groupproject.justeat.feature.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.RestaurantTable;
import com.groupproject.justeat.databinding.ActivityManageTablesBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageTablesActivity  extends BaseActivity<ActivityManageTablesBinding> {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;



    private DatabaseReference tablesReference;
    private ValueEventListener tablesListener;

    private List<RestaurantTable> tableList;
    private List<String> tableIds;
    private RestaurantTableAdapter adapter;

    @Override
    protected ActivityManageTablesBinding inflateBinding(
            LayoutInflater inflater) {
        return ActivityManageTablesBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        tablesReference  = firebaseDatabase.getReference("tables");

        tableList = new ArrayList<>();
        tableIds = new ArrayList<>();

        adapter = new RestaurantTableAdapter(
                tableList,
                tableIds,
                new RestaurantTableAdapter.OnTableClickListener() {

                    @Override
                    public void onEdit(String id, RestaurantTable table) {
                        editTable(id, table);
                    }

                    @Override
                    public void onDelete(String id, RestaurantTable table) {
                        deleteTable(id, table);
                    }
                }
        );

        getBinding().rvTables.setLayoutManager(
                new GridLayoutManager(this, 2)
        );

        getBinding().rvTables.setAdapter(adapter);
















        getBinding().btnAddTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        getApplicationContext(),
                        AddTableActivity.class
                );

                startActivity(intent);
            }
        });

        loadTables();
    }

    private void loadTables() {

        tablesListener = tablesReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        tableList.clear();
                        tableIds.clear();

                        for (DataSnapshot child : snapshot.getChildren()) {

                            RestaurantTable table =
                                    child.getValue(RestaurantTable.class);

                            String id = child.getKey();

                            if (table != null && id != null) {
                                tableList.add(table);
                                tableIds.add(id);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                ManageTablesActivity.this,
                                "Failed to load tables: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }



    private void editTable(String id, RestaurantTable table) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 0);

        EditText txtNumber = new EditText(this);
        txtNumber.setHint("Table Number");
        txtNumber.setText(table.getTableNumber());

        EditText txtCapacity = new EditText(this);
        txtCapacity.setHint("Capacity");
        txtCapacity.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtCapacity.setText(String.valueOf(table.getCapacity()));

        layout.addView(txtNumber);
        layout.addView(txtCapacity);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Table")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(unused -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String number =
                                txtNumber.getText().toString().trim();

                        String capacityText =
                                txtCapacity.getText().toString().trim();

                        if (number.isEmpty() || capacityText.isEmpty()) {
                            Toast.makeText(
                                    this,
                                    "Please enter all table details",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        int capacity;

                        try {
                            capacity = Integer.parseInt(capacityText);
                        } catch (NumberFormatException e) {
                            Toast.makeText(
                                    this,
                                    "Invalid capacity",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (capacity <= 0) {
                            Toast.makeText(
                                    this,
                                    "Capacity must be greater than 0",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("tableNumber", number);
                        updates.put("capacity", capacity);

                        tablesReference.child(id).updateChildren(updates)
                                .addOnSuccessListener(result -> {
                                    Toast.makeText(
                                            this,
                                            "Table updated successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(error -> {
                                    Toast.makeText(
                                            this,
                                            "Update failed: " + error.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });
                    });
        });

        dialog.show();
    }

    private void deleteTable(String id, RestaurantTable table) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Table")
                .setMessage(
                        "Are you sure you want to delete Table "
                                + table.getTableNumber() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    tablesReference.child(id).removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Table deleted successfully",
                                        Toast.LENGTH_SHORT
                                ).show();
                            })
                            .addOnFailureListener(error -> {
                                Toast.makeText(
                                        this,
                                        "Delete failed: " + error.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        if (tablesReference != null && tablesListener != null) {
            tablesReference.removeEventListener(tablesListener);
        }
        super.onDestroy();
    }
}