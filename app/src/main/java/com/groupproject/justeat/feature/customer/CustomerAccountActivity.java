package com.groupproject.justeat.feature.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.User;
import com.groupproject.justeat.databinding.ActivityCustomerAccountBinding;
import com.groupproject.justeat.feature.login.LoginActivity;
import com.groupproject.justeat.ui.dialog.ChangePasswordDialog;

import java.util.HashMap;
import java.util.Map;

public class CustomerAccountActivity extends BaseActivity<ActivityCustomerAccountBinding> {

    private DatabaseReference usersRef;

    @Override
    protected ActivityCustomerAccountBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCustomerAccountBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_customer_account);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initView();
        initData();
    }

    private void initView() {
        // Initialize your views here

        getBinding().btnChangePassword.setOnClickListener(v -> {
            ChangePasswordDialog dialog = ChangePasswordDialog.newInstance();
            dialog.show(getSupportFragmentManager(), ChangePasswordDialog.TAG);
        });

        getBinding().btnSave.setOnClickListener(v -> {
            saveProfile();
        });

        getBinding().btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        getBinding().bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, CustomerDashboardActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_my_menu) {
                startActivity(new Intent(this, CustomerMyMenuActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, CustomerOrdersActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_account) {
                return true;
            }
            return false;
        });

        getBinding().bottomNavigation.setSelectedItemId(R.id.nav_account);

    }
    private void initData() {
        setupFirebase();
        loadUserProfile();
    }
    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "Guest";
        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
    }
    private void loadUserProfile() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentuser = dataSnapshot.getValue(User.class);
                if (currentuser != null) {
                    getBinding().etFirstName.setText(currentuser.getFirstName());
                    getBinding().etLastName.setText(currentuser.getLastName());
                    getBinding().etEmail.setText(currentuser.getEmail());
                    getBinding().etPhone.setText(currentuser.getPhone().toString());
                    String avatarUrl = currentuser.getAvatarUrl();
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(CustomerAccountActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_restaurant_brand)
                                .error(R.drawable.ic_restaurant_brand)
                                .circleCrop()
                                .into(getBinding().imgAvatar);
                    } else {
                        getBinding().imgAvatar.setImageResource(R.drawable.ic_restaurant_brand);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CustomerAccountActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void saveProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String firstName = getBinding().etFirstName.getText().toString().trim();
        String lastName = getBinding().etLastName.getText().toString().trim();
        String phone = getBinding().etPhone.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(CustomerAccountActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);
        updates.put("updatedAt", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CustomerAccountActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CustomerAccountActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

