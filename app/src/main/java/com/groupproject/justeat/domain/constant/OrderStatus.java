package com.groupproject.justeat.domain.constant;

/** 整张订单状态在 Firebase 中保存的固定字符串。 */
public final class OrderStatus {
    /** 等待员工处理。 */
    public static final String PENDING = "PENDING";
    /** 餐厅已经接单。 */
    public static final String ACCEPTED = "ACCEPTED";
    /** 餐厅拒绝了订单。 */
    public static final String REJECTED = "REJECTED";
    /** 厨房正在制作。 */
    public static final String PREPARING = "PREPARING";
    /** 菜品已经可以上桌。 */
    public static final String READY = "READY";
    /** 订单已经完成。 */
    public static final String COMPLETED = "COMPLETED";
    /** 订单已经取消。 */
    public static final String CANCELLED = "CANCELLED";

    private OrderStatus() {
        throw new AssertionError("No instances");
    }
}

