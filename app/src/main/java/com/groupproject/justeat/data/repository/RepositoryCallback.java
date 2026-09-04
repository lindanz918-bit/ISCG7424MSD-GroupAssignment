package com.groupproject.justeat.data.repository;

/**
 * Repository 异步操作的统一回调，不向上层暴露 Firebase 的具体类型。
 *
 * @param <T> 成功时返回的数据类型
 */
public interface RepositoryCallback<T> {
    /**
     * 操作成功。
     *
     * @param data 返回的数据；Void 类型操作可传 null
     */
    void onSuccess(T data);

    /**
     * 操作失败。
     *
     * @param message 可展示给用户的错误信息
     */
    void onError(String message);
}

