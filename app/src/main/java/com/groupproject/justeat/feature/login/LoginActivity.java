package com.groupproject.justeat.feature.login;

import android.os.Bundle;
import android.content.Intent;
import com.groupproject.justeat.domain.constant.UserRole;
import com.groupproject.justeat.feature.customer.CustomerDashBoardActivity;
import com.groupproject.justeat.feature.dashboard.*;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.groupproject.justeat.R;
import com.groupproject.justeat.base.BaseActivity;
import com.groupproject.justeat.databinding.ActivityLoginBinding;
import com.groupproject.justeat.ui.dialog.MessageDialog;
import com.groupproject.justeat.util.GlideImageLoader;

/**
 * Firebase 邮箱密码登录页；按数据库角色进入对应 Dashboard。
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    /** 保存和发布登录页面状态。 */
    private LoginViewModel viewModel;

    @Override
    protected ActivityLoginBinding inflateBinding(LayoutInflater inflater) {
        return ActivityLoginBinding.inflate(inflater);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loadBrandImage();
        observeState();
        bindActions();
    }

    /** 使用 Glide 加载本地品牌图，页面离线时也可稳定显示。 */
    private void loadBrandImage() {
        GlideImageLoader.load(
                getBinding().ivBrand,
                R.drawable.ic_restaurant_brand,
                R.drawable.ic_image_placeholder,
                R.drawable.ic_image_placeholder
        );
    }

    /** 将 ViewModel 的只读状态连接到页面渲染方法。 */
    private void observeState() {
        viewModel.getLoading().observe(this, this::renderLoading);
        viewModel.getUiState().observe(this, this::renderState);
        viewModel.getErrorMessage().observe(this, message -> {
            if (message == null) return;
            if (getSupportFragmentManager().findFragmentByTag(MessageDialog.TAG) == null) {
                MessageDialog.newInstance(getString(R.string.login_failed_title), message)
                        .showNow(getSupportFragmentManager(), MessageDialog.TAG);
            }
            viewModel.onMessageShown();
        });
        viewModel.getDestinationRole().observe(this, role -> {
            if (role == null) return;
            Class<?> target;
            switch (role) {
                case UserRole.MANAGER: target = AdminDashBoardActivity.class; break;
                case UserRole.CUSTOMER: target = CustomerDashBoardActivity.class; break;
                case UserRole.STAFF: target = StaffDashBoardActivity.class; break;
                default: return;
            }
            viewModel.onNavigationHandled();
            getBinding().etPassword.setText("");
            startActivity(new Intent(this, target)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    /** 注册登录和注册提示操作。 */
    private void bindActions() {
        getBinding().btnLogin.setOnClickListener(ignored -> submitLogin());
        getBinding().tvSignUp.setOnClickListener(ignored -> showComingSoonDialog());
    }

    /** 读取当前输入并交给 ViewModel；密码不会保存为 Activity 字段。 */
    private void submitLogin() {
        CharSequence email = getBinding().etEmail.getText();
        CharSequence password = getBinding().etPassword.getText();
        viewModel.validateLogin(
                email == null ? "" : email.toString(),
                password == null ? "" : password.toString()
        );
    }

    /** 根据字段错误或成功状态更新页面。 */
    private void renderState(LoginUiState state) {
        getBinding().tilEmail.setError(state.getEmailErrorResId() == 0
                ? null : getString(state.getEmailErrorResId()));
        getBinding().tilPassword.setError(state.getPasswordErrorResId() == 0
                ? null : getString(state.getPasswordErrorResId()));
        if (state.getEmailErrorResId() != 0) {
            getBinding().etEmail.requestFocus();
        } else if (state.getPasswordErrorResId() != 0) {
            getBinding().etPassword.requestFocus();
        }
    }

    /** 加载时阻止重复点击，并显示进度指示器。 */
    private void renderLoading(Boolean loading) {
        boolean active = Boolean.TRUE.equals(loading);
        getBinding().progressLogin.setVisibility(active ? View.VISIBLE : View.GONE);
        getBinding().btnLogin.setEnabled(!active);
    }

    /** 提示注册页不在本次基础框架范围内。 */
    private void showComingSoonDialog() {
        MessageDialog.newInstance(
                getString(R.string.coming_soon_title),
                getString(R.string.coming_soon_message)
        ).show(getSupportFragmentManager(), MessageDialog.TAG);
    }
}
