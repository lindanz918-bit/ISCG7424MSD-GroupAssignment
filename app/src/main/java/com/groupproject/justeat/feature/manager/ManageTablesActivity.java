package com.groupproject.justeat.feature.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
        // Back Button
        getBinding().btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
                            if (table != null) {
                                Log.d("TableStatus", "Table: "
                                        + table.getTableNumber()
                                        + " Status: "
                                        + table.getOccupancyStatus());
                            }
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

        // Table Number Label
        TextView tvTableNumber = new TextView(this);
        tvTableNumber.setText("Table Number:");
        tvTableNumber.setTextSize(16);
        tvTableNumber.setPadding(0, 12, 0, 8);



        EditText txtNumber = new EditText(this);
        txtNumber.setHint("Table Number");
        txtNumber.setText(table.getTableNumber());

        // Capacity Label
        TextView tvCapacity = new TextView(this);
        tvCapacity.setText("Capacity:");
        tvCapacity.setTextSize(16);
        tvCapacity.setPadding(0, 20, 0, 8);

        NumberPicker numberPickerCapacity = new NumberPicker(this);
        numberPickerCapacity.setMinValue(1);
        numberPickerCapacity.setMaxValue(20);
        numberPickerCapacity.setValue(table.getCapacity());





        layout.addView(tvTableNumber);
        layout.addView(txtNumber);
        layout.addView(tvCapacity);
        layout.addView(numberPickerCapacity);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Table")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();


        dialog.setOnShowListener(unused -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String number =    txtNumber.getText().toString().trim();
//                        TextView tvTableNumber = new TextView(this);
//                        tvTableNumber.setText("Table Number:");
//                        tvTableNumber.setTextSize(16);
//                        tvTableNumber.setPadding(0, 12, 0, 8);
//                        layout.addView(tvTableNumber);
//
                        int capacityText =   numberPickerCapacity.getValue();
//                        TextView tvCapacity = new TextView(this);
//                        tvCapacity.setText("Capacity:");
//                        tvCapacity.setTextSize(16);
//                        tvCapacity.setPadding(0, 20, 0, 8);
//                        layout.addView(tvCapacity);
//                        layout.addView(numberPickerCapacity);

                        if (number.isEmpty()) {
                            Toast.makeText(
                                    this,
                                    "Please enter all table details",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }


                        Map<String, Object> updates = new HashMap<>();
                        updates.put("tableNumber", number);
                        updates.put("capacity", capacityText);
                        // 字段级更新保留 Staff 的 isEnabled 和 occupancyStatus，不覆盖整张桌。
                        updates.put("updatedAt", com.google.firebase.database.ServerValue.TIMESTAMP);

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
                            });
//
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
