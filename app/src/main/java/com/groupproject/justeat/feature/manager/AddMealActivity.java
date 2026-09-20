package com.groupproject.justeat.feature.manager;

import android.os.Bundle;
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

public class AddMealActivity extends AppCompatActivity {
    EditText txtImageUrl, txtMealName;
    EditText txtPrice, txtSpiciness, txtDescription;

    Button btnAdd;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_meal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Firebase

        firebaseDatabase =  FirebaseDatabase.getInstance();

        reference =    firebaseDatabase.getReference();


        // EditTexts

        txtImageUrl =   findViewById(R.id.txt_imageUrl);

        txtMealName =    findViewById(R.id.txt_mealname);

        txtPrice =    findViewById(R.id.txt_price);

        txtSpiciness =  findViewById(R.id.txt_Spiciness);

        txtDescription =     findViewById(R.id.txt_description);


        // Button

        btnAdd =       findViewById(R.id.btn_addMeal);


        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String imageUrl =
                        txtImageUrl.getText().toString();

                String mealName =
                        txtMealName.getText().toString();

                String price =
                        txtPrice.getText().toString();

                String spiciness =
                        txtSpiciness.getText().toString();

                String description =
                        txtDescription.getText().toString();


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

                String id =
                        reference.push().getKey();


                // Create Meal

                Meal meal = new Meal(
                        mealName,
                        price,
                        spiciness,
                        description,
                        imageUrl
                );


                // Save to Firebase

                reference
                        .child("meals")
                        .child(id)
                        .setValue(meal);


                Toast.makeText(
                        AddMealActivity.this,
                        "Meal added successfully",
                        Toast.LENGTH_LONG
                ).show();


                // Clear fields

                txtImageUrl.setText("");
                txtMealName.setText("");
                txtPrice.setText("");
                txtSpiciness.setText("");
                txtDescription.setText("");
            }
        });
    }
}