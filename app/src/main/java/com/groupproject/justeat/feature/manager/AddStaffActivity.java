package com.groupproject.justeat.feature.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.data.model.Staff;
import com.groupproject.justeat.databinding.ActivityAddStaffBinding;

public class AddStaffActivity extends BaseActivity<ActivityAddStaffBinding> {


    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    @Override
    protected ActivityAddStaffBinding inflateBinding(LayoutInflater inflater) {
        return ActivityAddStaffBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();

        //Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();
        getBinding().btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getBinding().btnAddStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String imageUrl =  getBinding().txtImageUrl.getText().toString();
                String lastName =  getBinding().txtLastName.getText().toString();

                String firstName =      getBinding().txtFirstName.getText().toString();

                String email =    getBinding().txtEmail.getText().toString();

                String password =  getBinding().txtPassword.getText().toString();


                if (imageUrl.isEmpty()
                        || lastName.isEmpty()
                        || firstName.isEmpty()
                        || email.isEmpty()
                        || password.isEmpty()) {

                    Toast.makeText(
                            AddStaffActivity.this,
                            "Please enter all meal details",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                //Generate ID
                String id = reference.push().getKey();

                //Create staff
                Staff staff = new Staff(
                        imageUrl,
                        firstName,
                        lastName,
                        email
                );

                //Save to Firebase
                reference
                        .child("Staff")
                        .child(id)
                        .setValue(staff);

                Toast.makeText(
                        AddStaffActivity.this,
                        "Staff added successfully",
                        Toast.LENGTH_LONG
                ).show();

                //Clear fields
                getBinding().txtImageUrl.setText("");
                getBinding().txtLastName.setText("");
                getBinding().txtFirstName.setText("");
                getBinding().txtEmail.setText("");

                getBinding().txtPassword.setText("");
            }
        });


    }

    private void initView() {
        // Initialize views here
        getBinding().btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }

    private void initData() {

    }
}