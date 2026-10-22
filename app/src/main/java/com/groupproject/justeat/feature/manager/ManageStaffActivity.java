package com.groupproject.justeat.feature.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.Staff;
import com.groupproject.justeat.databinding.ActivityManageStaffBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import androidx.appcompat.app.AlertDialog;


public class ManageStaffActivity
        extends BaseActivity<ActivityManageStaffBinding> {

    private ManageStaffAdapter adapter;
    private List<Staff> staffList;
    private List<String> staffIds;
    private DatabaseReference staffRef;
    private ValueEventListener staffListener;


    @Override
    protected ActivityManageStaffBinding inflateBinding(LayoutInflater inflater) {
        return ActivityManageStaffBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(
                getBinding().getRoot(), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
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

        // Initialize staff list
        staffList = new ArrayList<>();
        staffIds = new ArrayList<>();

        // Firebase
        staffRef = FirebaseDatabase.getInstance()
                .getReference("Staff");

        // Adapter
        adapter = new ManageStaffAdapter(
                staffList,
                new ManageStaffAdapter.onItemClickListener() {

                    @Override
                    public void onItemClick(int position) {

                        if (position >= 0 && position < staffIds.size()) {

                            String staffId = staffIds.get(position);
                            Staff staff = staffList.get(position);

                            deleteStaff(staffId, staff);
                        }
                    }
                }
        );

        // RecyclerView
        getBinding().rvStaff.setLayoutManager(
                new LinearLayoutManager(this)
        );

        getBinding().rvStaff.setAdapter(adapter);


        loadStaff();
        getBinding().btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getBinding().btnAddStaff.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(
                                ManageStaffActivity.this,
                                AddStaffActivity.class
                        );

                        startActivity(intent);
                    }
                }
        );


    }

    private void loadStaff() {

        staffListener = staffRef.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        staffList.clear();
                        staffIds.clear();

                        for (DataSnapshot child : snapshot.getChildren()) {

                            Staff staff = child.getValue(Staff.class);
                            String id = child.getKey();

                            if (staff != null && id != null) {

                                staffList.add(staff);
                                staffIds.add(id);
                            }
                        }

                        adapter.updateData(new ArrayList<>(staffList));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(
                                ManageStaffActivity.this,
                                "Failed to load staff: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
    private void deleteStaff(String id, Staff staff) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Staff")
                .setMessage(
                        "Are you sure you want to delete "
                                + staff.getFirstName() + " "
                                + staff.getLastName() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    staffRef.child(id).removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Staff deleted successfully",
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

        if (staffRef != null && staffListener != null) {
            staffRef.removeEventListener(staffListener);
        }

        super.onDestroy();
    }
}