package com.groupproject.justeat.data.model;

/** 顾客订单主记录，订单明细通过 orderId 在独立节点中关联。 */
public class Order {
    /** 订单唯一 ID。 */
    private String orderId;

    /** 下单顾客的用户 ID。 */
    private String customerId;

    /** 就餐餐桌 ID。 */
    private String tableId;

    /** 页面显示人数；Firebase 中人数保存为数字。 */
    private String guestCount;

    /** 就餐日期，格式 yyyy-MM-dd。 */
    private String bookingDate;

    /** 订单状态，值来自 OrderStatus。 */
    private String status;
    private boolean deletedByCustomer;

    /** 创建时间，Unix 毫秒时间戳。 */
    private long createdAt;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public Order() {}

    /** 创建包含全部订单字段的实体。 */
    public Order(
            String orderId,
            String customerId,
            String guestCount,
            String bookingDate,
            String tableId,
            String status,
            boolean deletedByCustomer,
            long createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.guestCount = guestCount;
        this.bookingDate = bookingDate;
        this.tableId = tableId;
        this.status = status;
        this.deletedByCustomer = deletedByCustomer;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(String guestCount) {
        this.guestCount = guestCount;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public static boolean isActive(String status) {
        return "ACCEPTED".equals(status) || "PREPARING".equals(status) || "READY".equals(status);
    }

    public boolean isDeletedByCustomer() {
        return deletedByCustomer;
    }
    public static boolean canComplete(String status, boolean allServed) {
        return isActive(status) && allServed;
    }

    public static boolean canStaffChange(String currentStatus, String targetStatus) {
        if ("PENDING".equals(currentStatus)) {
            return "ACCEPTED".equals(targetStatus) || "REJECTED".equals(targetStatus);
        }
        if ("ACCEPTED".equals(currentStatus)
                || "PREPARING".equals(currentStatus)
                || "READY".equals(currentStatus)) {
            return "CANCELLED".equals(targetStatus);
        }
        return false;
    }

    /** 未结束的订单显示取消操作；已完成订单保留预约名额。 */
    public boolean canCancel() {
        return "PENDING".equals(status)
                || "ACCEPTED".equals(status)
                || "PREPARING".equals(status)
                || "READY".equals(status);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public void setDeletedByCustomer(boolean deletedByCustomer) {
        this.deletedByCustomer = deletedByCustomer;
    }
    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
