package com.groupproject.justeat.feature.login;

import android.os.Bundle;
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
 * 应用当前唯一实现的页面：邮箱密码登录表单。
 * 本阶段只做本地校验，不访问 Firebase，也不进行页面跳转。
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
        if (state.isValidationSuccessful() && state.getMessageResId() != 0) {
            MessageDialog.newInstance(
                    getString(R.string.validation_complete_title),
                    getString(state.getMessageResId())
            ).show(getSupportFragmentManager(), MessageDialog.TAG);
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
