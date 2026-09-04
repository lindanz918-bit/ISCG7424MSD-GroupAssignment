package com.groupproject.justeat.data.model;

/** 顾客订桌记录，使用 ID 引用用户和餐桌，避免 Firebase 多层嵌套。 */
public class Booking {
    /** 预订唯一 ID。 */
    private String bookingId;
    /** 发起预订的顾客用户 ID。 */
    private String customerId;
    /** 被预订的餐桌 ID。 */
    private String tableId;
    /** 到店人数。 */
    private int guestCount;
    /** 计划到店时间，Unix 毫秒时间戳。 */
    private long bookingTimeMillis;
    /** 顾客备注。 */
    private String notes;
    /** 预订状态，值来自 BookingStatus。 */
    private String status;
    /** 创建时间，Unix 毫秒时间戳。 */
    private long createdAt;
    /** 最后更新时间，Unix 毫秒时间戳。 */
    private long updatedAt;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public Booking() {
    }

    /** 创建包含全部订桌字段的实体。 */
    public Booking(String bookingId, String customerId, String tableId,
                   int guestCount, long bookingTimeMillis, String notes, String status,
                   long createdAt, long updatedAt) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.tableId = tableId;
        this.guestCount = guestCount;
        this.bookingTimeMillis = bookingTimeMillis;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public long getBookingTimeMillis() {
        return bookingTimeMillis;
    }

    public void setBookingTimeMillis(long bookingTimeMillis) {
        this.bookingTimeMillis = bookingTimeMillis;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

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

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

