package com.groupproject.justeat.feature.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.groupproject.justeat.R;
import com.groupproject.justeat.domain.constant.UserRole;
import com.groupproject.justeat.feature.staff.StaffOrderActivity;
import com.groupproject.justeat.feature.staff.StaffProfileActivity;
import com.groupproject.justeat.feature.staff.StaffTableActivity;

/** 保留原有角色校验，并把 Staff 功能入口放入校验后的 Dashboard。 */
public class StaffDashBoardActivity extends BaseDashboardActivity {
    @Override protected String requiredRole() { return UserRole.STAFF; }
    @Override protected int titleResource() { return R.string.staff_dashboard; }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addActionButton(R.string.staff_orders_action,
                v -> startActivity(new Intent(this, StaffOrderActivity.class)));
        addActionButton(R.string.staff_tables_action,
                v -> startActivity(new Intent(this, StaffTableActivity.class)));
        addActionButton(R.string.staff_profile_action,
                v -> startActivity(new Intent(this, StaffProfileActivity.class)));
    }

    private void addActionButton(int textResId, android.view.View.OnClickListener listener) {
        MaterialButton button = new MaterialButton(this);
        button.setText(textResId);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        getBinding().dashboardContent.addView(button, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}
