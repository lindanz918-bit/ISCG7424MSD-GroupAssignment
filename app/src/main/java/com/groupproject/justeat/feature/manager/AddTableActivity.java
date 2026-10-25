package com.groupproject.justeat.feature.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.data.model.RestaurantTable;
import com.groupproject.justeat.databinding.ActivityAddTableBinding;
import com.google.firebase.database.ValueEventListener;


public class AddTableActivity extends BaseActivity<ActivityAddTableBinding> {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;



    @Override
    protected ActivityAddTableBinding inflateBinding(LayoutInflater inflater) {
        return ActivityAddTableBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);


        ViewCompat.setOnApplyWindowInsetsListener(
                getBinding().getRoot(), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        getBinding().numberPickerCapacity.setMinValue(1);
        getBinding().numberPickerCapacity.setMaxValue(20);
        getBinding().numberPickerCapacity.setValue(4);
        // Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();


        getBinding().btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getBinding().btnSaveTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String tableNumber = getBinding().txtTableNumber.getText().toString().trim();

                int capacity = getBinding().numberPickerCapacity.getValue();
                String statusText = "Available";

                if (getBinding().radioAvailable.isChecked()) {
                    statusText = "Available";
                } else {
                    statusText = "Occupied";
                }
//                int capacity = Integer.parseInt(capacityText);
                String finalStatusText = statusText;




                if (tableNumber.isEmpty()) {

                    Toast.makeText(
                            AddTableActivity.this,
                            "Please enter all table details",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }



                String id = reference.push().getKey();


                DatabaseReference tablesRef = reference.child("tables");


                tablesRef.orderByChild("tableNumber").equalTo(tableNumber).get()
                        .addOnSuccessListener(snapshot -> {

                            if (snapshot.exists()) {
                                Toast.makeText(AddTableActivity.this,
                                        "Table number already exists",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Map<String, Object> table = newTableFields(tableNumber, capacity, finalStatusText);

                            tablesRef.push().setValue(table)
                                    .addOnSuccessListener(unused -> {

                                        Toast.makeText(AddTableActivity.this,
                                                "Table added successfully",
                                                Toast.LENGTH_SHORT).show();

                                        getBinding().txtTableNumber.setText("");
                                        getBinding().numberPickerCapacity.setValue(1);
                                        getBinding().radioAvailable.setChecked(true);
                                    }).addOnFailureListener(error -> Toast.makeText(AddTableActivity.this,
                                            "Could not save table. Please retry.", Toast.LENGTH_LONG).show());
//
                        });


//



//            RestaurantTable table = new RestaurantTable(
//                    tableNumber,
//                    capacity,
//                    statusText
//            );
//
//            //Save to Firebase
//             reference.child("tables").child(id).setValue(table);
//                Toast.makeText(
//            AddTableActivity.this,
//                    "Table added successfully",
//            Toast.LENGTH_LONG
//                 ).show();

            //Clear fields
//            getBinding().txtTableNumber.setText("");
//            getBinding().numberPickerCapacity.setValue(1);
//                getBinding().radioAvailable.setChecked(true);
            }


        });
    }
    /** 现场 Occupied 不等于禁止未来预约；不再写旧 status。 */
    static Map<String, Object> newTableFields(String number, int capacity, String status) {
        Map<String, Object> table = new HashMap<>();
        table.put("tableNumber", number);
        table.put("capacity", capacity);
        table.put("isEnabled", true);
        table.put("occupancyStatus", status.toUpperCase(Locale.ROOT));
        table.put("createdAt", ServerValue.TIMESTAMP);
        table.put("updatedAt", ServerValue.TIMESTAMP);
        return table;
    }
}
