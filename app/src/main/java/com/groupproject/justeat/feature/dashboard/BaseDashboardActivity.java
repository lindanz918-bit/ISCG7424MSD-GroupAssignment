package com.groupproject.justeat.feature.dashboard;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.databinding.ActivityDashboardBinding;
import com.groupproject.justeat.data.model.User;
import com.groupproject.justeat.domain.constant.UserStatus;
import com.groupproject.justeat.feature.login.LoginActivity;
import com.groupproject.justeat.util.FirebaseAuthClient;

/** 简单 Dashboard：每次进入前重新检查会话与角色，业务权限仍由数据库规则控制。 */
public abstract class BaseDashboardActivity extends BaseActivity<ActivityDashboardBinding> {
    private final FirebaseAuthClient authClient = new FirebaseAuthClient();
    private int request;
    protected abstract String requiredRole();
    protected abstract int titleResource();
    @Override protected ActivityDashboardBinding inflateBinding(LayoutInflater inflater) {
        return ActivityDashboardBinding.inflate(inflater);
    }
    @Override protected void onStart() {
        super.onStart();
        int current = ++request;
        getBinding().dashboardContent.setVisibility(View.GONE);
        getBinding().progressDashboard.setVisibility(View.VISIBLE);
        authClient.loadCurrentUser(new FirebaseAuthClient.Callback<User>() {
            public void onSuccess(User user) {
                if (current != request || isFinishing() || isDestroyed()) return;
                if (!requiredRole().equals(user.getRole()) || !UserStatus.ACTIVE.equals(user.getStatus())) {
                    logout(); return;
                }
                getBinding().progressDashboard.setVisibility(View.GONE);
                getBinding().dashboardContent.setVisibility(View.VISIBLE);
                getBinding().dashboardTitle.setText(titleResource());
                getBinding().dashboardEmail.setText(user.getEmail());
                getBinding().btnLogout.setOnClickListener(v -> logout());
            }
            public void onError(String message) {
                if (current == request && !isFinishing() && !isDestroyed()) logout();
            }
        });
    }
    @Override protected void onStop() { request++; super.onStop(); }
    private void logout() {
        request++;
        authClient.signOut(new FirebaseAuthClient.Callback<Void>() {
            public void onSuccess(Void ignored) { returnToLogin(); }
            public void onError(String ignored) { returnToLogin(); }
        });
    }
    private void returnToLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
