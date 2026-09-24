package com.groupproject.justeat.feature.manager;

public class Meal {
    private String imageUrl;
    private String mealName;
    private String price;
    private Object spiciness;
    private String description;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public Meal() {}
    public Meal(String imageUrl, String mealName, String price, Object spiciness, String description) {
        this.imageUrl = imageUrl;
        this.mealName = mealName;
        this.price = price;
        this.spiciness = spiciness;
        this.description = description;
    }
}
