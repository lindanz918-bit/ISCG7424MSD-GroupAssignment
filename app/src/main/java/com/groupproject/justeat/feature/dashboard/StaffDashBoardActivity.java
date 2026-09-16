package com.groupproject.justeat.feature.dashboard;

import com.groupproject.justeat.R;
import com.groupproject.justeat.domain.constant.UserRole;

public class StaffDashBoardActivity extends BaseDashboardActivity {
    @Override protected String requiredRole() { return UserRole.STAFF; }
    @Override protected int titleResource() { return R.string.staff_dashboard; }
}
