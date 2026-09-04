package com.groupproject.justeat.domain.constant;

/** 单个订单明细状态在 Firebase 中保存的固定字符串。 */
public final class OrderItemStatus {
    /** 等待厨房处理。 */
    public static final String PENDING = "PENDING";
    /** 厨房正在制作。 */
    public static final String PREPARING = "PREPARING";
    /** 菜品已经上桌。 */
    public static final String SERVED = "SERVED";
    /** 明细已被取消。 */
    public static final String CANCELLED = "CANCELLED";

    private OrderItemStatus() {
        throw new AssertionError("No instances");
    }
}
