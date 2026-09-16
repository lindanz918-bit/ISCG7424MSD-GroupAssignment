package com.groupproject.justeat.feature.dashboard;

import com.groupproject.justeat.R;
import com.groupproject.justeat.domain.constant.UserRole;

public class AdminDashBoardActivity extends BaseDashboardActivity {
    @Override protected String requiredRole() { return UserRole.MANAGER; }
    @Override protected int titleResource() { return R.string.admin_dashboard; }
}
