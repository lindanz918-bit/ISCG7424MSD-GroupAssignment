package com.groupproject.justeat.data.repository;

import com.groupproject.justeat.data.model.User;

/** 用户认证数据源边界，未来可由 Firebase Authentication 实现。 */
public interface AuthRepository {
    /**
     * 使用邮箱和密码登录。
     *
     * @param email 登录邮箱
     * @param password 登录密码；实现类不得记录或持久化该值
     * @param callback 返回登录用户或错误
     */
    void signIn(String email, String password, RepositoryCallback<User> callback);

    /**
     * 退出当前账号。
     *
     * @param callback 返回完成状态或错误
     */
    void signOut(RepositoryCallback<Void> callback);

    /**
     * 获取当前 Firebase 用户 ID。
     *
     * @return 已登录用户 ID；未登录时返回 null
     */
    String getCurrentUserId();

    /**
     * 判断当前是否存在登录会话。
     *
     * @return true 表示已登录
     */
    boolean isSignedIn();
}

