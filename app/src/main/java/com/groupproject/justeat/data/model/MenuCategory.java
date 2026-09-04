package com.groupproject.justeat.data.model;

/** 菜单分类实体，用于对菜品进行扁平化分组和排序。 */
public class MenuCategory {
    /** 分类唯一 ID。 */
    private String categoryId;
    /** 分类显示名称。 */
    private String name;
    /** 分类说明。 */
    private String description;
    /** 分类在菜单中的显示顺序，数值越小越靠前。 */
    private int displayOrder;
    /** 分类是否对顾客开放。 */
    private boolean active;
    /** 创建时间，Unix 毫秒时间戳。 */
    private long createdAt;
    /** 最后更新时间，Unix 毫秒时间戳。 */
    private long updatedAt;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public MenuCategory() {
    }

    /** 创建包含全部菜单分类字段的实体。 */
    public MenuCategory(String categoryId, String name, String description,
                        int displayOrder, boolean active, long createdAt, long updatedAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

