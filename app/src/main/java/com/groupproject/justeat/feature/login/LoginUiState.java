package com.groupproject.justeat.feature.login;

/**
 * 登录页不可变渲染状态。
 * 字符串资源 ID 使用 0 表示对应位置没有需要展示的信息。
 */
public final class LoginUiState {
    /** 当前是否正在处理登录请求。 */
    private final boolean loading;
    /** 邮箱输入框错误字符串资源 ID。 */
    private final int emailErrorResId;
    /** 密码输入框错误字符串资源 ID。 */
    private final int passwordErrorResId;
    /** 通用提示字符串资源 ID。 */
    private final int messageResId;
    /** 本地输入校验是否成功。 */
    private final boolean validationSuccessful;

    /** 创建包含全部字段的登录页状态。 */
    public LoginUiState(boolean loading, int emailErrorResId, int passwordErrorResId,
                        int messageResId, boolean validationSuccessful) {
        this.loading = loading;
        this.emailErrorResId = emailErrorResId;
        this.passwordErrorResId = passwordErrorResId;
        this.messageResId = messageResId;
        this.validationSuccessful = validationSuccessful;
    }

    /** @return 尚未提交时的初始状态 */
    public static LoginUiState idle() {
        return new LoginUiState(false, 0, 0, 0, false);
    }

    /** @return 正在处理输入时的状态 */
    public static LoginUiState loading() {
        return new LoginUiState(true, 0, 0, 0, false);
    }

    /**
     * 创建字段校验失败状态。
     *
     * @param emailErrorResId 邮箱错误资源 ID，0 表示无错误
     * @param passwordErrorResId 密码错误资源 ID，0 表示无错误
     * @return 校验失败状态
     */
    public static LoginUiState validationError(int emailErrorResId, int passwordErrorResId) {
        return new LoginUiState(false, emailErrorResId, passwordErrorResId, 0, false);
    }

    /**
     * 创建本地校验成功状态。
     *
     * @param messageResId 成功提示资源 ID
     * @return 校验成功状态
     */
    public static LoginUiState success(int messageResId) {
        return new LoginUiState(false, 0, 0, messageResId, true);
    }

    public boolean isLoading() {
        return loading;
    }

    public int getEmailErrorResId() {
        return emailErrorResId;
    }

    public int getPasswordErrorResId() {
        return passwordErrorResId;
    }

    public int getMessageResId() {
        return messageResId;
    }

    public boolean isValidationSuccessful() {
        return validationSuccessful;
    }
}

