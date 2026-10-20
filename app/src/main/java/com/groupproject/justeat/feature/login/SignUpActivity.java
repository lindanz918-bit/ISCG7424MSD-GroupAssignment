package com.groupproject.justeat.feature.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.databinding.ActivitySignUpBinding;
import com.groupproject.justeat.domain.constant.UserRole;
import com.groupproject.justeat.feature.customer.CustomerDashboardActivity;
import com.groupproject.justeat.feature.dashboard.StaffDashBoardActivity;
import com.groupproject.justeat.feature.manager.ManagerDashboardActivity;
import com.groupproject.justeat.ui.dialog.MessageDialog;

public class SignUpActivity extends BaseActivity<ActivitySignUpBinding> {

    private SignUpViewModel viewModel;

    @Override
    protected ActivitySignUpBinding inflateBinding(LayoutInflater inflater) {
        return ActivitySignUpBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        observeState();
        bindActions();
    }

    private void observeState() {
        viewModel.getLoading().observe(this, this::renderLoading);
        viewModel.getUiState().observe(this, this::renderState);
        viewModel.getErrorMessage().observe(this, message -> {
            if (message == null) return;
            if (getSupportFragmentManager().findFragmentByTag(MessageDialog.TAG) == null) {
                MessageDialog.newInstance("Sign Up Failed", message)
                        .showNow(getSupportFragmentManager(), MessageDialog.TAG);
            }
            viewModel.onMessageShown();
        });

        viewModel.getDestinationRole().observe(this, role -> {
            if (role == null) return;
            Class<?> target;
            switch (role) {
                case UserRole.MANAGER: target = ManagerDashboardActivity.class; break;
                case UserRole.CUSTOMER: target = CustomerDashboardActivity.class; break;
                case UserRole.STAFF: target = StaffDashBoardActivity.class; break;
                default: target = CustomerDashboardActivity.class; break;
            }
            viewModel.onNavigationHandled();
            startActivity(new Intent(this, target)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    private void bindActions() {
        getBinding().btnBack.setOnClickListener(ignored -> finish());
        getBinding().btnSignUp.setOnClickListener(ignored -> submitSignUp());
    }

    private void submitSignUp() {
        String firstName = getTextString(getBinding().etFirstName);
        String lastName = getTextString(getBinding().etLastName);
        String email = getTextString(getBinding().etEmail);
        String password = getTextString(getBinding().etPassword);

        String selectedRole = UserRole.CUSTOMER; // default role
        int checkedChipId = getBinding().chipGroupRole.getCheckedChipId();
        if (checkedChipId == R.id.chipStaff) {
            selectedRole = UserRole.STAFF;
        } else if (checkedChipId == R.id.chipManager) {
            selectedRole = UserRole.MANAGER;
        }

        viewModel.registerUser(firstName, lastName, email, password, selectedRole);
    }

    private String getTextString(com.google.android.material.textfield.TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString().trim();
    }

    private void renderLoading(Boolean loading) {
        boolean active = Boolean.TRUE.equals(loading);
        getBinding().progressSignUp.setVisibility(active ? View.VISIBLE : View.GONE);
        getBinding().btnSignUp.setEnabled(!active);
    }

    private void renderState(SignUpUiState state) {
        getBinding().tilFirstName.setError(state.getFirstNameErrorResId() == 0
                ? null : getString(state.getFirstNameErrorResId()));
        getBinding().tilLastName.setError(state.getLastNameErrorResId() == 0
                ? null : getString(state.getLastNameErrorResId()));
        getBinding().tilEmail.setError(state.getEmailErrorResId() == 0
                ? null : getString(state.getEmailErrorResId()));
        getBinding().tilPassword.setError(state.getPasswordErrorResId() == 0
                ? null : getString(state.getPasswordErrorResId()));

        // 自动聚焦到第一个出错的输入框
        if (state.getFirstNameErrorResId() != 0) {
            getBinding().etFirstName.requestFocus();
        } else if (state.getLastNameErrorResId() != 0) {
            getBinding().etLastName.requestFocus();
        } else if (state.getEmailErrorResId() != 0) {
            getBinding().etEmail.requestFocus();
        } else if (state.getPasswordErrorResId() != 0) {
            getBinding().etPassword.requestFocus();
        }
    }
}