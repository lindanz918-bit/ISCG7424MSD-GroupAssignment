package com.groupproject.justeat.feature.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.databinding.ActivityCustomerMenuItemDetailsBinding;

public class CustomerMenuItemDetailsActivity extends BaseActivity<ActivityCustomerMenuItemDetailsBinding> {

    private String mealName, imageUrl, spiciness, price, description;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    @Override
    protected ActivityCustomerMenuItemDetailsBinding inflateBinding(LayoutInflater inflater) {
        return ActivityCustomerMenuItemDetailsBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_customer_menu_item_details);
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initView();
        init_data();
    }

    private void initView() {
        getBinding().btnBack.setOnClickListener(v -> {
            finish();
        });

        getBinding().btnBook.setOnClickListener(v -> {
            saveMenuItemToFirebase();
            Intent intent = new Intent(this, CustomerMyMenuActivity.class);
            startActivity(intent);
        });

        getBinding().bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_search) {
                Toast.makeText(this, "Dashboard Selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CustomerDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_my_menu) {
                Toast.makeText(this, "My Menu Selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CustomerMyMenuActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, CustomerOrdersActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_account) {
                Toast.makeText(this, "Account Selected", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });


    }

    private void init_data() {
        Intent intent = getIntent();
        if (intent != null)
        {
            mealName = intent.getStringExtra("MENU_ITEM_NAME");
            imageUrl = intent.getStringExtra("MENU_ITEM_IMAGE");
            spiciness = intent.getStringExtra("MENU_ITEM_SPICINESS");
            price = intent.getStringExtra("MENU_ITEM_PRICE");
            description = intent.getStringExtra("MENU_ITEM_DESCRIPTION");
            // String category = intent.getStringExtra("MENU_ITEM_CATEGORY");
            // String discount = intent.getStringExtra("MENU_ITEM_DISCOUNT");

            // Set the data to the views
            Glide.with(getBinding().imgBanner.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder) // optional placeholder
                    .error(R.drawable.ic_image_placeholder)      // optional error image
                    .into(getBinding().imgBanner);
            getBinding().tvName.setText(mealName);
            getBinding().tvSpiciness.setText(spiciness);
            getBinding().tvPrice.setText(price);
            getBinding().tvDescription.setText(description);
            // getBinding().tvCategory.setText(category);
            // getBinding().tvDiscount.setText(discount);
        }
    }

    private void saveMenuItemToFirebase(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = (currentUser != null) ? currentUser.getUid() : null;
        DatabaseReference userMenuRef = mDatabase.child("MyMenu").child(userId);
        String menuItemId = userMenuRef.push().getKey();

        if (menuItemId == null) {
            Toast.makeText(this, "Failed to generate menu item ID", Toast.LENGTH_SHORT).show();
            return;
        }

        MenuItem item = new MenuItem(menuItemId, mealName, imageUrl, spiciness, price, description);
        getBinding().btnBook.setEnabled(false);
        userMenuRef.child(menuItemId).setValue(item).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Menu item saved successfully", Toast.LENGTH_SHORT).show();
                    getBinding().btnBook.setEnabled(true);
                    finish();
                    //Intent intent = new Intent(this, CustomerDashboardActivity.class, CustomerMyMenuActivity.class);
                   // StartActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save menu item", Toast.LENGTH_SHORT).show();
                    getBinding().btnBook.setEnabled(true);
                });
    }
}