package com.groupproject.justeat.feature.dashboard;

import com.groupproject.justeat.R;
import com.groupproject.justeat.domain.constant.UserRole;

public class CustomerDashBoardActivity extends BaseDashboardActivity {
    @Override protected String requiredRole() { return UserRole.CUSTOMER; }
    @Override protected int titleResource() { return R.string.customer_dashboard; }
}
