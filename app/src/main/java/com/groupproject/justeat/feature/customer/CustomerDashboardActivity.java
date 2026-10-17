package com.groupproject.justeat.feature.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
import com.groupproject.justeat.databinding.ActivityCustomerDashboardBinding;
import com.groupproject.justeat.data.model.MenuItem;


import java.util.ArrayList;
import java.util.List;

public class CustomerDashboardActivity extends BaseActivity<ActivityCustomerDashboardBinding> {
    private CustomerMealAdapter adapter;
    private List<MenuItem> mealList;
    private DatabaseReference mealsRef;

    @Override
    protected ActivityCustomerDashboardBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCustomerDashboardBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // setContentView(R.layout.activity_customer_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mealList = new ArrayList<>();
        mealsRef = FirebaseDatabase.getInstance().getReference("meals");

        initView();
        initData();
    }
    private void initView() {
        getBinding().svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return true;
            }
        });

        adapter = new CustomerMealAdapter(mealList, new CustomerMealAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                MenuItem selectedMeal = mealList.get(position);
                Toast.makeText(CustomerDashboardActivity.this, "Selected: " + selectedMeal.getmealName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CustomerDashboardActivity.this, CustomerMenuItemDetailsActivity.class);
                intent.putExtra("MENU_ITEM_IMAGE", selectedMeal.getImageUrl());
                intent.putExtra("MENU_ITEM_NAME", selectedMeal.getmealName());
                intent.putExtra("MENU_ITEM_SPICINESS", selectedMeal.getSpiciness().toString());
                intent.putExtra("MENU_ITEM_PRICE", selectedMeal.getPrice());
                intent.putExtra("MENU_ITEM_DESCRIPTION", selectedMeal.getDescription());
//                intent.putExtra("MENU_ITEM_CATEGORY", selectedMeal.getCategory());
//                intent.putExtra("MENU_ITEM_DISCOUNT", selectedMeal.getDiscount());
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        getBinding().bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
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
                startActivity(new Intent(this, CustomerAccountActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        getBinding().bottomNavigation.setSelectedItemId(R.id.nav_search);
        getBinding().fabBookTable.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, CustomerBookTableActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        getBinding().rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvSearchResults.setAdapter(adapter);
    }
    private void initData() {
        Log.d("FirebaseDebug", "Listening on path: " + mealsRef.toString());
        mealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("FirebaseDebug", "onDataChange called. Exists: " + dataSnapshot.exists() + ", Children count: " + dataSnapshot.getChildrenCount());
                mealList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    MenuItem meal = childSnapshot.getValue(MenuItem.class);
                    if (meal != null) {
                        mealList.add(meal);
                        Log.d("FirebaseDebug", "Parsed Meal: " + meal.getmealName());
                    } else {
                        Log.e("FirebaseDebug", "Failed to parse snapshot: " + childSnapshot.toString());
                    }
                }
                Log.d("FirebaseDebug", "Total meals in list: " + mealList.size());
                adapter.updateData(mealList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Read data from Firebase Error: " + error.getMessage());
            }
        });
    }
}