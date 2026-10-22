package com.groupproject.justeat.feature.manager;

import static android.content.Intent.getIntent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupproject.justeat.R;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.databinding.ActivityEditMenuItemBinding;

import java.util.HashMap;
import java.util.Map;

public class EditMenuItemActivity  extends BaseActivity<ActivityEditMenuItemBinding> {

    private DatabaseReference mealRef;
    private boolean spicinessIsNumber = false;

    @Override
    protected ActivityEditMenuItemBinding inflateBinding(LayoutInflater inflater) {
        return ActivityEditMenuItemBinding.inflate(inflater);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Meal ID
        String mealId = getIntent().getStringExtra("mealId");

        if (mealId == null || mealId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Meal ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Firebase
        mealRef = FirebaseDatabase.getInstance()
                .getReference("meals")
                .child(mealId);
// Back Button
        getBinding().btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // Load original meal data
        loadMeal();

        // Save Button
        getBinding().btnSaveMeal.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        saveMeal();
                    }
                }
        );
        // Delete Button


        getBinding().btnDeleteMeal.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        new AlertDialog.Builder(EditMenuItemActivity.this)
                                .setTitle("Delete Meal")
                                .setMessage("Are you sure you want to delete this meal?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    deleteMeal();
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();
                    }
                }
        );

    }

    private void loadMeal() {

        mealRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        MenuItem meal =
                                snapshot.getValue(MenuItem.class);

                        if (meal != null) {

                            getBinding().txtMealName.setText(
                                    meal.getmealName()
                            );

                            getBinding().txtPrice.setText(
                                    meal.getPrice()
                            );

                            getBinding().txtSpiciness.setText(
                                    meal.getSpiciness() != null
                                            ? String.valueOf(meal.getSpiciness())
                                            : ""
                            );

                            getBinding().txtDescription.setText(
                                    meal.getDescription()
                            );

                            getBinding().txtImageUrl.setText(
                                    meal.getImageUrl()
                            );
// Load Meal Image
                            Glide.with(EditMenuItemActivity.this)
                                    .load(meal.getImageUrl())
                                    .placeholder(R.drawable.ic_restaurant_brand)
                                    .error(R.drawable.ic_restaurant_brand)
                                    .into(getBinding().imgMealPreview);

                            Object value =
                                    snapshot.child("spiciness").getValue();

                            spicinessIsNumber = value instanceof Number;

                        } else {

                            Toast.makeText(
                                    EditMenuItemActivity.this,
                                    "Meal not found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Toast.makeText(
                                EditMenuItemActivity.this,
                                "Failed to load meal",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

    }

    private void saveMeal() {

        String mealName = getBinding().txtMealName
                .getText().toString().trim();

        String price = getBinding().txtPrice
                .getText().toString().trim();

        String spiciness = getBinding().txtSpiciness
                .getText().toString().trim();

        String description = getBinding().txtDescription
                .getText().toString().trim();

        String imageUrl = getBinding().txtImageUrl
                .getText().toString().trim();

        if (mealName.isEmpty()
                || price.isEmpty()
                || spiciness.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please fill in all required fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Map<String, Object> updates = new HashMap<>();

        updates.put("mealName", mealName);
        updates.put("price", price);
        updates.put("description", description);
        updates.put("imageUrl", imageUrl);

        if (spicinessIsNumber) {

            try {
                updates.put("spiciness",
                        Integer.parseInt(spiciness));
            } catch (NumberFormatException e) {

                Toast.makeText(
                        this,
                        "Please enter a valid spiciness number",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

        } else {
            updates.put("spiciness", spiciness);
        }

        mealRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(
                            EditMenuItemActivity.this,
                            "Meal updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });
//                .addOnFailureListener(e -> {
//
//                    Toast.makeText(
//                            EditMenuItemActivity.this,
//                            "Failed to update meal: " + e.getMessage(),
//                            Toast.LENGTH_SHORT
//                    ).show();
//                });
    }

    // Delete Meal from Firebase
    private void deleteMeal() {
        mealRef.removeValue()
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(
                            EditMenuItemActivity.this,
                            "Meal deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });
//                .addOnFailureListener(e -> {
//
//                    Toast.makeText(
//                            EditMenuItemActivity.this,
//                            "Failed to delete meal: " + e.getMessage(),
//                            Toast.LENGTH_SHORT
//                    ).show();
//                });


    }
}