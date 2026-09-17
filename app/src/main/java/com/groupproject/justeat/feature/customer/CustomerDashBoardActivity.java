package com.groupproject.justeat.feature.customer;

import com.groupproject.justeat.R;
import com.groupproject.justeat.domain.constant.UserRole;
import com.groupproject.justeat.feature.dashboard.BaseDashboardActivity;

public class CustomerDashBoardActivity extends BaseDashboardActivity {
    @Override protected String requiredRole() { return UserRole.CUSTOMER; }
    @Override protected int titleResource() { return R.string.customer_dashboard; }
}
