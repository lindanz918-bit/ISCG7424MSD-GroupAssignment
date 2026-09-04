package com.groupproject.justeat.feature.login;

import com.groupproject.justeat.R;

import java.util.regex.Pattern;

/**
 * 登录输入校验器，只处理字符串规则，不访问 Activity、网络或 Firebase。
 */
public final class LoginValidator {

    /** 基础邮箱格式；忽略大小写。 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 校验邮箱和密码。
     *
     * @param email 用户输入的邮箱，可以为 null
     * @param password 用户输入的密码，可以为 null；不会被保存或记录
     * @return 可直接交给 ViewModel 发布的登录状态
     */
    public LoginUiState validate(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim();
        String rawPassword = password == null ? "" : password;

        int emailErrorResId = 0;
        if (normalizedEmail.isEmpty()) {
            emailErrorResId = R.string.error_email_required;
        } else if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            emailErrorResId = R.string.error_email_invalid;
        }

        int passwordErrorResId = 0;
        if (rawPassword.trim().isEmpty()) {
            passwordErrorResId = R.string.error_password_required;
        } else if (rawPassword.length() < 6) {
            passwordErrorResId = R.string.error_password_too_short;
        }

        if (emailErrorResId != 0 || passwordErrorResId != 0) {
            return LoginUiState.validationError(emailErrorResId, passwordErrorResId);
        }
        return LoginUiState.success(R.string.validation_success_message);
    }
}
