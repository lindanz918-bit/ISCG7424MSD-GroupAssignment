package com.groupproject.justeat.domain.constant;

/** 订桌记录状态在 Firebase 中保存的固定字符串。 */
public final class BookingStatus {
    /** 等待员工确认。 */
    public static final String PENDING = "PENDING";
    /** 员工已经确认。 */
    public static final String CONFIRMED = "CONFIRMED";
    /** 员工拒绝了预订。 */
    public static final String REJECTED = "REJECTED";
    /** 顾客或员工已取消。 */
    public static final String CANCELLED = "CANCELLED";
    /** 顾客已经完成到店用餐。 */
    public static final String COMPLETED = "COMPLETED";

    private BookingStatus() {
        throw new AssertionError("No instances");
    }
}

