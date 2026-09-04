package com.groupproject.justeat.domain.constant;

/** 餐桌状态在 Firebase 中保存的固定字符串。 */
public final class TableStatus {
    /** 当前可以预订或入座。 */
    public static final String AVAILABLE = "AVAILABLE";
    /** 当前已有顾客用餐。 */
    public static final String OCCUPIED = "OCCUPIED";
    /** 已被顾客预订。 */
    public static final String RESERVED = "RESERVED";
    /** 因维护等原因不可使用。 */
    public static final String UNAVAILABLE = "UNAVAILABLE";

    private TableStatus() {
        throw new AssertionError("No instances");
    }
}

