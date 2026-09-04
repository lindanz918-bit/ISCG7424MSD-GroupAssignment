package com.groupproject.justeat.data.repository;

import com.groupproject.justeat.data.model.MenuItem;

import java.util.List;

/** 菜单数据操作边界，供顾客浏览和经理维护菜品。 */
public interface MenuRepository {
    /**
     * 加载全部菜品。
     *
     * @param callback 返回菜品列表或错误
     */
    void getMenuItems(RepositoryCallback<List<MenuItem>> callback);

    /**
     * 新增或更新菜品。
     *
     * @param item 要保存的菜品
     * @param callback 返回已保存菜品或错误
     */
    void saveMenuItem(MenuItem item, RepositoryCallback<MenuItem> callback);

    /**
     * 删除指定菜品。
     *
     * @param menuItemId 菜品 ID
     * @param callback 返回完成状态或错误
     */
    void deleteMenuItem(String menuItemId, RepositoryCallback<Void> callback);
}

