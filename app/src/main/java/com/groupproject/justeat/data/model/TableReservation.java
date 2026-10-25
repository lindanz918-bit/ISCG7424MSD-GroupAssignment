package com.groupproject.justeat.data.model;

/**
 * 一桌一天的预约名额，保存于 tableReservations/{yyyy-MM-dd}/{tableKey}。
 * 日期和餐桌 key 已在路径中，不重复保存为字段。
 * 顾客、人数和订单状态等详情仍从 Orders/{orderId} 读取。
 * 创建时必须与订单一起原子写入，并由 Firebase 权限规则防止重复预约。
 * 拒绝或取消时删除对应名额；完成订单仍保留当天名额。
 */
public class TableReservation {
    /** 占用该日期餐桌的订单 ID，对应 Orders 节点下的真实 key。 */
    private String orderId;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public TableReservation() {
    }

    /** 创建名额；orderId 应使用创建订单时生成的 key。 */
    public TableReservation(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
