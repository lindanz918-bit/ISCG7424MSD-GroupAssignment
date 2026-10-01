package com.groupproject.justeat.data.model;

/**
 * 系统用户实体。顾客、员工和经理共用此结构，并通过 role 字段区分权限。
 */
public class User {
    /** Firebase Authentication 用户唯一 ID。 */
    private String userId;
    /** 名。 */
    private String firstName;
    /** 姓。 */
    private String lastName;
    /** 登录邮箱。 */
    private String email;
    /** 联系电话。 */
    private Object phone;
    /** 头像图片地址。 */
    private String avatarUrl;
    /** 用户角色，值来自 UserRole。 */
    private String role;
    /** 账号状态，值来自 UserStatus。 */
    private String status;
    /** 创建时间，Unix 毫秒时间戳。 */
    private long createdAt;
    /** 最后更新时间，Unix 毫秒时间戳。 */
    private long updatedAt;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public User() {
    }

    /** 创建包含全部用户字段的实体。 */
    public User(String userId, String firstName, String lastName, String email,
                Object phone, String avatarUrl, String role, String status,
                long createdAt, long updatedAt) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Object getPhone() {
        return phone;
    }

    public void setPhone(Object phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

