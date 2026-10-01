package com.groupproject.justeat.data.model;

/**
 * 顾客订单主记录，订单明细通过 orderId 在独立节点中关联。
 */
public class Order {
    /**
     * 订单唯一 ID。
     */
    private String orderId;
    /**
     * 下单顾客的用户 ID。
     */
    private String customerId;
    /** 可选的关联预订 ID。 */
//    private String bookingId;
    /**
     * 就餐餐桌 ID。
     */
    private String tableId;
    /** 订单总额，单位为分。 */
    //private long totalInCents;
    /** 顾客对整张订单的特殊要求。 */
    //private String specialInstructions;
    /**
     * 订单状态，值来自 OrderStatus。
     */

    private String guestCount;
    private String bookingDate;
    private String status;
    /**
     * 创建时间，Unix 毫秒时间戳。
     */
    private long createdAt;
    /** 最后更新时间，Unix 毫秒时间戳。 */
    //private long updatedAt;

    /**
     * Firebase 反序列化需要的公开无参构造方法。
     */
    public Order() {
    }
    /**
     * 创建包含全部订单字段的实体。
     */
//    public Order(String orderId, String customerId, String bookingId, String tableId,
//                 long totalInCents, String specialInstructions, String status,
//                 long createdAt, long updatedAt) {
//        this.orderId = orderId;
//        this.customerId = customerId;
//        this.bookingId = bookingId;
//        this.tableId = tableId;
//        this.totalInCents = totalInCents;
//        this.specialInstructions = specialInstructions;
//        this.status = status;
//        this.createdAt = createdAt;
//        this.updatedAt = updatedAt;
//    }
    public Order(String orderId, String customerId, String guestCount, String bookingDate, String tableId,
                 String status, long createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.guestCount = guestCount;
        this.bookingDate = bookingDate;
        this.tableId = tableId;
        this.status = status;
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

//    public String getBookingId() {
//        return bookingId;
//    }
//
//    public void setBookingId(String bookingId) {
//        this.bookingId = bookingId;
//    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

//    public long getTotalInCents() {
//        return totalInCents;
//    }

//    public void setTotalInCents(long totalInCents) {
//        this.totalInCents = totalInCents;
//    }

//    public String getSpecialInstructions() {
//        return specialInstructions;
//    }

//    public void setSpecialInstructions(String specialInstructions) {
//        this.specialInstructions = specialInstructions;
//    }

    public String getGuestCount() { return guestCount; }
    public void setGuestCount(String guestCount) { this.guestCount = guestCount; }

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

//    public long getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(long updatedAt) {
//        this.updatedAt = updatedAt;
//    }
}

