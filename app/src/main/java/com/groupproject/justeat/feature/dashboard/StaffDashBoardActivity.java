package com.groupproject.justeat.feature.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.databinding.ActivityStaffDashboardBinding;
import com.groupproject.justeat.domain.constant.UserRole;
import com.groupproject.justeat.domain.constant.UserStatus;
import com.groupproject.justeat.feature.login.LoginActivity;
import com.groupproject.justeat.feature.staff.*;

/** Staff 独立导航容器，不影响其他角色的 BaseDashboardActivity。 每次回到前台重新校验角色与 ACTIVE 状态；未通过前隐藏整个工作区。 */
public class StaffDashBoardActivity extends BaseActivity<ActivityStaffDashboardBinding> {
    private int request;
    private boolean authorized;
    private boolean leaving;
    private int selectedTab = R.id.staff_nav_home;
    private String accountEmail = "";

    @Override
    protected ActivityStaffDashboardBinding inflateBinding(LayoutInflater inflater) {
        return ActivityStaffDashboardBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(Bundle state) {
        setTheme(R.style.Theme_JustEat_Staff);
        super.onCreate(state);
        if (state != null) selectedTab = state.getInt("staff.selectedTab", R.id.staff_nav_home);
        getBinding()
                .staffNavigation
                .setOnItemSelectedListener(
                        item -> {
                            if (!authorized) return false;
                            showTab(item.getItemId());
                            return true;
                        });
        getBinding()
                .staffNavigation
                .setOnItemReselectedListener(
                        item -> {
                            /* Keep scroll/input state. */
                        });
        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                if (selectedTab != R.id.staff_nav_home && authorized)
                                    selectTab(R.id.staff_nav_home);
                                else {
                                    setEnabled(false);
                                    getOnBackPressedDispatcher().onBackPressed();
                                }
                            }
                        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        int current = ++request;
        authorized = false;
        getBinding().staffWorkspace.setVisibility(View.GONE);
        getBinding().staffAuthProgress.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            signOut();
            return;
        }
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener(
                        snapshot -> {
                            if (current != request || isFinishing() || isDestroyed() || leaving)
                                return;
                            if (!uid.equals(FirebaseAuth.getInstance().getUid())
                                    || !UserRole.STAFF.equals(snapshot.child("role").getValue())
                                    || !UserStatus.ACTIVE.equals(
                                            snapshot.child("status").getValue())) {
                                signOut();
                                return;
                            }
                            accountEmail = snapshot.child("email").getValue(String.class);
                            if (accountEmail == null) accountEmail = "";
                            authorized = true;
                            getBinding().staffAuthProgress.setVisibility(View.GONE);
                            getBinding().staffWorkspace.setVisibility(View.VISIBLE);
                            showTab(selectedTab);
                            getBinding()
                                    .staffNavigation
                                    .getMenu()
                                    .findItem(selectedTab)
                                    .setChecked(true);
                            Fragment home =
                                    getSupportFragmentManager()
                                            .findFragmentByTag("staff:" + R.id.staff_nav_home);
                            if (home instanceof StaffHomeFragment)
                                ((StaffHomeFragment) home).refreshAccount();
                        })
                .addOnFailureListener(
                        e -> {
                            if (current == request && !isFinishing() && !isDestroyed()) signOut();
                        });
    }

    /** Home 快捷入口与底部导航共用同一选择逻辑，不新建 Activity。 */
    public void selectTab(int id) {
        if (authorized) getBinding().staffNavigation.setSelectedItemId(id);
    }

    private void showTab(int id) {
        if (getSupportFragmentManager().isStateSaved()) return;
        selectedTab = id;
        String tag = "staff:" + id;
        Fragment target = getSupportFragmentManager().findFragmentByTag(tag);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != target)
                tx.hide(fragment).setMaxLifecycle(fragment, Lifecycle.State.STARTED);
        }
        if (target == null) {
            target =
                    id == R.id.staff_nav_orders
                            ? new StaffOrderFragment()
                            : id == R.id.staff_nav_tables
                                    ? new StaffTableFragment()
                                    : id == R.id.staff_nav_profile
                                            ? new StaffProfileFragment()
                                            : new StaffHomeFragment();
            tx.add(R.id.staffFragmentContainer, target, tag);
        } else tx.show(target);
        tx.setMaxLifecycle(target, Lifecycle.State.RESUMED)
                .setPrimaryNavigationFragment(target)
                .commitNow();
        // Hide keyboard when leaving an editable page.
        View focus = getCurrentFocus();
        if (focus != null) {
            ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(focus.getWindowToken(), 0);
            focus.clearFocus();
        }
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void signOut() {
        if (leaving) return;
        leaving = true;
        authorized = false;
        request++;
        getBinding().staffWorkspace.setVisibility(View.GONE);
        FirebaseAuth.getInstance().signOut();
        returnToLogin();
    }

    private void returnToLogin() {
        startActivity(
                new Intent(this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        out.putInt("staff.selectedTab", selectedTab);
        super.onSaveInstanceState(out);
    }

    @Override
    protected void onStop() {
        request++;
        authorized = false;
        super.onStop();
    }
}
