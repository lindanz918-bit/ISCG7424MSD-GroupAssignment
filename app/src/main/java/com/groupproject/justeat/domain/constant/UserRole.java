package com.groupproject.justeat.domain.constant;

/** 用户角色在 Firebase 中保存的固定字符串。 */
public final class UserRole {
    /** 普通顾客。 */
    public static final String CUSTOMER = "CUSTOMER";
    /** 餐厅员工。 */
    public static final String STAFF = "STAFF";
    /** 餐厅经理。 */
    public static final String MANAGER = "MANAGER";

    private UserRole() {
        throw new AssertionError("No instances");
    }
}

