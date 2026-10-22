package com.groupproject.justeat.feature.staff;

import com.groupproject.justeat.domain.constant.OrderStatus;

/** 员工订单状态转换规则；最终权限仍由 Firebase Security Rules 强制执行。 */
public final class StaffOrderActionPolicy {
    public static final String ACTION_ACCEPT = "Accept";
    public static final String ACTION_REJECT = "Reject";
    public static final String ACTION_CANCEL = "Cancel";

    private StaffOrderActionPolicy() {
    }

    public static boolean isActive(String status) {
        return OrderStatus.ACCEPTED.equals(status) || OrderStatus.PREPARING.equals(status)
                || OrderStatus.READY.equals(status);
    }

    public static boolean canComplete(String status, boolean allServed) {
        return isActive(status) && allServed;
    }

    public static boolean canApply(String currentStatus, String targetStatus) {
        if (OrderStatus.PENDING.equals(currentStatus)) {
            return OrderStatus.ACCEPTED.equals(targetStatus)
                    || OrderStatus.REJECTED.equals(targetStatus);
        }
        if (OrderStatus.ACCEPTED.equals(currentStatus)
                || OrderStatus.PREPARING.equals(currentStatus)
                || OrderStatus.READY.equals(currentStatus)) {
            return OrderStatus.CANCELLED.equals(targetStatus);
        }
        return false;
    }

    public static String actionLabel(String targetStatus) {
        if (OrderStatus.ACCEPTED.equals(targetStatus)) return ACTION_ACCEPT;
        if (OrderStatus.REJECTED.equals(targetStatus)) return ACTION_REJECT;
        if (OrderStatus.CANCELLED.equals(targetStatus)) return ACTION_CANCEL;
        return "Update";
    }
}
