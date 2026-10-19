package com.groupproject.justeat.feature.manager;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.databinding.ActivityManagerDashboardBinding;
import com.groupproject.justeat.feature.customer.CustomerAccountActivity;
import com.groupproject.justeat.feature.customer.CustomerBookTableActivity;
import com.groupproject.justeat.feature.customer.CustomerDashboardActivity;
import com.groupproject.justeat.feature.customer.CustomerMyMenuActivity;
import com.groupproject.justeat.feature.customer.CustomerOrdersActivity;

public class ManagerDashboardActivity
        extends BaseActivity<ActivityManagerDashboardBinding> {

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
                        AddMealActivity.class
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

    }

}