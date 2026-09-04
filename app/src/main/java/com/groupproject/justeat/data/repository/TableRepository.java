package com.groupproject.justeat.data.repository;

import com.groupproject.justeat.data.model.RestaurantTable;

import java.util.List;

/** 餐桌数据操作边界，供顾客查看和员工、经理管理餐桌。 */
public interface TableRepository {
    /**
     * 加载全部餐桌。
     *
     * @param callback 返回餐桌列表或错误
     */
    void getTables(RepositoryCallback<List<RestaurantTable>> callback);

    /**
     * 新增或更新餐桌。
     *
     * @param table 要保存的餐桌
     * @param callback 返回已保存餐桌或错误
     */
    void saveTable(RestaurantTable table, RepositoryCallback<RestaurantTable> callback);

    /**
     * 删除指定餐桌。
     *
     * @param tableId 餐桌 ID
     * @param callback 返回完成状态或错误
     */
    void deleteTable(String tableId, RepositoryCallback<Void> callback);
}

