package com.groupproject.justeat.feature.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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
import com.groupproject.justeat.databinding.ActivityAddMealBinding;
import com.groupproject.justeat.databinding.ActivityCustomerBookTableBinding;

public class AddMealActivity extends BaseActivity<ActivityAddMealBinding> {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;

    @Override
    protected ActivityAddMealBinding inflateBinding(LayoutInflater inflater) {
        return ActivityAddMealBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(
                getBinding().getRoot(),
                (v, insets) -> {
                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();

        // Add Meal Button
        getBinding().btnAddMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String imageUrl =  getBinding().txtImageUrl.getText().toString();

                String mealName =      getBinding().txtMealname.getText().toString();

                String price =    getBinding().txtPrice.getText().toString();

                String spiciness =  getBinding().txtSpiciness.getText().toString();

                String description =       getBinding().txtDescription.getText().toString();

                if (imageUrl.isEmpty()
                        || mealName.isEmpty()
                        || price.isEmpty()
                        || spiciness.isEmpty()
                        || description.isEmpty()) {

                    Toast.makeText(
                            AddMealActivity.this,
                            "Please enter all meal details",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                // Generate ID
                String id = reference.push().getKey();

                // Create MenuItem
                MenuItem menuItem = new MenuItem(
                        id,
                        mealName,
                        imageUrl,
                        spiciness,
                        price,
                        description
                );

                //Save to Firebase
                reference
                        .child("meals")
                        .child(id)
                        .setValue(menuItem);

                Toast.makeText(
                        AddMealActivity.this,
                        "Meal added successfully",
                        Toast.LENGTH_LONG
                ).show();

                //Clear fields
                getBinding().txtImageUrl.setText("");
                getBinding().txtMealname.setText("");
                getBinding().txtPrice.setText("");
                getBinding().txtSpiciness.setText("");
                getBinding().txtDescription.setText("");
            }
        });
    }
}