package com.groupproject.justeat.data.repository;

import com.groupproject.justeat.data.model.Order;

import java.util.List;

/** 订单数据操作边界，支持顾客下单和员工处理订单。 */
public interface OrderRepository {
    /**
     * 加载某位顾客的全部订单。
     *
     * @param customerId 顾客用户 ID
     * @param callback 返回订单列表或错误
     */
    void getOrdersForCustomer(String customerId,
                              RepositoryCallback<List<Order>> callback);

    /**
     * 按状态加载订单，供员工处理待确认或制作中的订单。
     *
     * @param status 订单状态，值来自 OrderStatus
     * @param callback 返回订单列表或错误
     */
    void getOrdersByStatus(String status, RepositoryCallback<List<Order>> callback);

    /**
     * 新增或更新订单。
     *
     * @param order 要保存的订单
     * @param callback 返回已保存订单或错误
     */
    void saveOrder(Order order, RepositoryCallback<Order> callback);

    /**
     * 更新订单状态。
     *
     * @param orderId 订单 ID
     * @param status 新状态，值来自 OrderStatus
     * @param callback 返回完成状态或错误
     */
    void updateOrderStatus(String orderId, String status,
                           RepositoryCallback<Void> callback);
}
