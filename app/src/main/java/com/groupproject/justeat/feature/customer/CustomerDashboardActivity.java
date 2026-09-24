package com.groupproject.justeat.feature.customer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.groupproject.justeat.feature.manager.Meal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class CustomerDashboardActivity extends BaseActivity<ActivityCustomerDashboardBinding> {
    private MainAdapter adapter;
    private List<Meal> mealList;
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
        // Initialize your views here
        getBinding().btnAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            }
        });

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

        adapter = new MainAdapter(mealList, new MainAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Meal selectedMeal = mealList.get(position);
                Toast.makeText(CustomerDashboardActivity.this, "Selected: " + selectedMeal.getMealName(), Toast.LENGTH_SHORT).show();
            }
        });

        getBinding().rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvSearchResults.setAdapter(adapter);
    }

    private void initData() {
        Log.d("FirebaseDebug", "Listening on path: " + mealsRef.toString());
        mealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("FirebaseDebug", "onDataChange called. Exists: " + dataSnapshot.exists() + ", Children count: " + dataSnapshot.getChildrenCount());
                mealList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Meal meal = childSnapshot.getValue(Meal.class);
                    if (meal != null) {
                        mealList.add(meal);
                        Log.d("FirebaseDebug", "Parsed Meal: " + meal.getMealName());
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