package com.groupproject.justeat.feature.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
//import android.view.MenuItem;
import com.groupproject.justeat.data.model.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.groupproject.justeat.databinding.ActivityManagerDashboardBinding;

import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardActivity
        extends BaseActivity<ActivityManagerDashboardBinding> {
    private ManagerMealAdapter adapter;
    private List<MenuItem> mealList;
    private List<String> mealIds;
    private DatabaseReference mealsRef;
    private ValueEventListener mealsListener;

    @Override
    protected ActivityManagerDashboardBinding inflateBinding(LayoutInflater inflater) {
        return ActivityManagerDashboardBinding.inflate(inflater);
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

        getBinding(). manageMenuCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        getApplicationContext(),
                        ManageMenuActivity.class
                );

                startActivity(intent);
            }
        });

        getBinding(). manageTableCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        getApplicationContext(),
                        ManageTablesActivity.class
                );

                startActivity(intent);
            }
        });

        getBinding(). manageStaffCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        getApplicationContext(),
                        AddStaffActivity.class
                );

                startActivity(intent);
            }
        });


        getBinding().bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ManagerProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });


        getBinding().bottomNavigation.setSelectedItemId(R.id.nav_home);




        // Initialize meal list
        mealList = new ArrayList<>();
        mealIds = new ArrayList<>();

// Firebase
        mealsRef = FirebaseDatabase.getInstance()
                .getReference("meals");

// Adapter
        adapter = new ManagerMealAdapter(
                mealList,
                new ManagerMealAdapter.onItemClickListener() {
                    @Override
                    public void onItemClick(int position) {

                        if (position < 0 || position >= mealIds.size()) {
                            return;
                        }

                        String mealId = mealIds.get(position);

                        Intent intent = new Intent(
                                ManagerDashboardActivity.this,
                                EditMenuItemActivity.class
                        );

                        intent.putExtra("mealId", mealId);

                        startActivity(intent);
                    }
                }
        );

// RecyclerView
        getBinding().rvMeals.setLayoutManager(
                new LinearLayoutManager(this)
        );

        getBinding().rvMeals.setAdapter(adapter);

// Load Meals
        loadMeals();

    }

    private void loadMeals() {
        mealsListener = mealsRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mealList.clear();
                        mealIds.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {

                            MenuItem meal = child.getValue(MenuItem.class);
                            String id = child.getKey();

                            if (meal != null && id != null) {

                                mealList.add(meal);
                                mealIds.add(id);
                            }
                        }
                        adapter.updateData(new ArrayList<>(mealList));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(
                                ManagerDashboardActivity.this,
                                "Failed to load meals: " + error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}