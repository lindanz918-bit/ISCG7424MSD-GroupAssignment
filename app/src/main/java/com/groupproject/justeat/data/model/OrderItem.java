package com.groupproject.justeat.data.model;

/** 订单中的单项菜品，保存名称和价格快照以保留历史订单内容。 */
public class OrderItem {
    /** 订单明细唯一 ID。 */
    private String orderItemId;
    /** 所属订单 ID。 */
    private String orderId;
    /** 原始菜品 ID。 */
    private String menuItemId;
    /** 下单时的菜品名称快照。 */
    private String menuItemName;
    /** 下单时的单价快照，单位为分。 */
    private long unitPriceInCents;
    /** 购买数量。 */
    private int quantity;
    /** 本明细小计，单位为分。 */
    private long subtotalInCents;
    /** 针对该菜品的顾客备注。 */
    private String notes;
    /** 出餐状态，值来自 OrderItemStatus。 */
    private String status;
    /** 创建时间，Unix 毫秒时间戳。 */
    private long createdAt;
    /** 最后更新时间，Unix 毫秒时间戳。 */
    private long updatedAt;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public OrderItem() {
    }

    /** 创建包含全部订单明细字段的实体。 */
    public OrderItem(String orderItemId, String orderId, String menuItemId,
                     String menuItemName, long unitPriceInCents, int quantity,
                     long subtotalInCents, String notes, String status,
                     long createdAt, long updatedAt) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.unitPriceInCents = unitPriceInCents;
        this.quantity = quantity;
        this.subtotalInCents = subtotalInCents;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public long getUnitPriceInCents() {
        return unitPriceInCents;
    }

    public void setUnitPriceInCents(long unitPriceInCents) {
        this.unitPriceInCents = unitPriceInCents;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getSubtotalInCents() {
        return subtotalInCents;
    }

    public void setSubtotalInCents(long subtotalInCents) {
        this.subtotalInCents = subtotalInCents;
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
