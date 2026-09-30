package com.groupproject.justeat.data.model;

import com.google.firebase.database.PropertyName;

/** 菜品实体，保存顾客浏览和经理维护菜单所需的数据。 */
public class MenuItem {
    /** 菜品唯一 ID。 */
    private String menuItemId;
    /** 所属菜单分类 ID。 */
    //private String categoryId;
    /** 菜品名称。 */
    private String mealName;
    /** 菜品描述和主要配料。 */
    private String description;
    /** 菜品图片地址。 */
    private String imageUrl;
    /** 价格，单位为分，避免浮点金额误差。 */
    //private long priceInCents;
    private String price;
    /** 辣度等级，建议取值 0 至 5。 */
    //private int spicinessLevel;
    private Object spiciness;
    /** 当前是否可以下单。 */
    //private boolean available;
    /** 创建时间，Unix 毫秒时间戳。 */
    //private long createdAt;
    /** 最后更新时间，Unix 毫秒时间戳。 */
    //private long updatedAt;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public MenuItem() {
    }

    /** 创建包含全部菜品字段的实体。 */
//    public MenuItem(String menuItemId, String categoryId, String name,
//                    String description, String imageUrl, long priceInCents,
//                    int spicinessLevel, boolean available, long createdAt, long updatedAt) {
//        this.menuItemId = menuItemId;
//        this.categoryId = categoryId;
//        this.name = name;
//        this.description = description;
//        this.imageUrl = imageUrl;
//        this.priceInCents = priceInCents;
//        this.spicinessLevel = spicinessLevel;
//        this.available = available;
//        this.createdAt = createdAt;
//        this.updatedAt = updatedAt;
//    }
    public MenuItem(String id, String mealName, String imageUrl, String spiciness, String price, String description) {
        this.menuItemId = id;
        this.mealName = mealName;
        this.imageUrl = imageUrl;
        this.spiciness = spiciness;
        this.price = price;
        this.description = description;
    }
    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

//    public String getCategoryId() {
//        return categoryId;
//    }

//    public void setCategoryId(String categoryId) {
//        this.categoryId = categoryId;
//    }

    public String getmealName() {
        return mealName;
    }

    public void setmealName(String mealName) {
        this.mealName = mealName;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Object getSpiciness() {
        return spiciness;
    }

    public void setSpiciness(Object spiciness) {
        this.spiciness = spiciness;
    }

    //    public long getPriceInCents() {
//        return priceInCents;
//    }

//    public void setPriceInCents(long priceInCents) {
//        this.priceInCents = priceInCents;
//    }

//    public int getSpicinessLevel() {
//        return spicinessLevel;
//    }

//    public void setSpicinessLevel(int spicinessLevel) {
//        this.spicinessLevel = spicinessLevel;
//    }

//    public boolean isAvailable() {
//        return available;
//    }

//    public void setAvailable(boolean available) {
//        this.available = available;
//    }

//    public long getCreatedAt() {
//        return createdAt;
//    }

//    public void setCreatedAt(long createdAt) {
//        this.createdAt = createdAt;
//    }

//    public long getUpdatedAt() {
//        return updatedAt;
//    }

//    public void setUpdatedAt(long updatedAt) {
//        this.updatedAt = updatedAt;
//    }
}
