package com.groupproject.justeat.domain.constant;

/** 用户账号状态在 Firebase 中保存的固定字符串。 */
public final class UserStatus {
    /** 账号可以正常使用。 */
    public static final String ACTIVE = "ACTIVE";
    /** 账号被经理暂时停用。 */
    public static final String SUSPENDED = "SUSPENDED";
    /** 账号已被禁用。 */
    public static final String DISABLED = "DISABLED";

    private UserStatus() {
        throw new AssertionError("No instances");
    }
}

