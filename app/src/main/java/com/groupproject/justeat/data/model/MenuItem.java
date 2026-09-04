package com.groupproject.justeat.data.model;

/** 菜品实体，保存顾客浏览和经理维护菜单所需的数据。 */
public class MenuItem {
    /** 菜品唯一 ID。 */
    private String menuItemId;
    /** 所属菜单分类 ID。 */
    private String categoryId;
    /** 菜品名称。 */
    private String name;
    /** 菜品描述和主要配料。 */
    private String description;
    /** 菜品图片地址。 */
    private String imageUrl;
    /** 价格，单位为分，避免浮点金额误差。 */
    private long priceInCents;
    /** 辣度等级，建议取值 0 至 5。 */
    private int spicinessLevel;
    /** 当前是否可以下单。 */
    private boolean available;
    /** 创建时间，Unix 毫秒时间戳。 */
    private long createdAt;
    /** 最后更新时间，Unix 毫秒时间戳。 */
    private long updatedAt;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public MenuItem() {
    }

    /** 创建包含全部菜品字段的实体。 */
    public MenuItem(String menuItemId, String categoryId, String name,
                    String description, String imageUrl, long priceInCents,
                    int spicinessLevel, boolean available, long createdAt, long updatedAt) {
        this.menuItemId = menuItemId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.priceInCents = priceInCents;
        this.spicinessLevel = spicinessLevel;
        this.available = available;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getPriceInCents() {
        return priceInCents;
    }

    public void setPriceInCents(long priceInCents) {
        this.priceInCents = priceInCents;
    }

    public int getSpicinessLevel() {
        return spicinessLevel;
    }

    public void setSpicinessLevel(int spicinessLevel) {
        this.spicinessLevel = spicinessLevel;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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
