package com.groupproject.justeat.feature.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.groupproject.justeat.databinding.ActivityCustomerMyMenuBinding;

import java.util.ArrayList;
import java.util.List;


public class CustomerMyMenuActivity extends BaseActivity<ActivityCustomerMyMenuBinding> {

    private  CustomerMyMenuAdapter adapter;
    private final List<MenuItem> menuList = new ArrayList<>();
    private DatabaseReference mDatabase;
    @Override
    protected ActivityCustomerMyMenuBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCustomerMyMenuBinding.inflate(inflater);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_customer_my_menu);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initView();
        init_data();
    }

    private void initView() {
        getBinding().btnBookTable.setOnClickListener(view -> {
//            if (menuList.isEmpty()) {
//                Toast.makeText(this, "Your saved menu is empty. Please add items first.", Toast.LENGTH_SHORT).show();
//                return;
//            }

            Intent intent = new Intent(this, CustomerBookTableActivity.class);
            startActivity(intent);

        });

        adapter = new CustomerMyMenuAdapter(this, menuList, (item, position) ->  {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(CustomerMyMenuActivity.this)
                    .setTitle("Delete Meal")
                    .setMessage("Are you sure you want to delete \"" + item.getmealName() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        menuList.remove(item);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        getBinding().rvMyMenu.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvMyMenu.setAdapter(adapter);
        getBinding().bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, CustomerDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_my_menu) {
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, CustomerOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                Toast.makeText(this, "Account Selected", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });
        getBinding().bottomNavigation.setSelectedItemId(R.id.nav_my_menu);
    }
    private void init_data() {
        setupFirebase();
        loadSavedMenuItems();
    }
    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : "Guest";
        mDatabase = FirebaseDatabase.getInstance().getReference("MyMenu").child(userId);
    }

    private void loadSavedMenuItems() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuList.clear();
                 for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                     MenuItem item = snapshot.getValue(MenuItem.class);
                     if (item != null) {
                         menuList.add(item);
                     }
                 }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CustomerMyMenuActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteMenuItem(MenuItem item) {
        if (item.getMenuItemId() != null) {
            mDatabase.child(item.getMenuItemId()).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(CustomerMyMenuActivity.this, "Menu item deleted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(CustomerMyMenuActivity.this, "Failed to delete menu item", Toast.LENGTH_SHORT).show());
        }

    }

}