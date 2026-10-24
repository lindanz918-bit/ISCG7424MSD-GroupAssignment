package com.groupproject.justeat.data.model;

/** 餐桌实体；日期预约名额另存于 tableReservations，不使用旧 status 字段。 */
public class RestaurantTable {
    /** 展示桌号，不是 Firebase 节点 key。 */
    private String tableNumber;
    /** 最大容纳人数。 */
    private int capacity;
    /** 现场状态：AVAILABLE / OCCUPIED，与未来日期是否可订无关。 */
    private String occupancyStatus;
    /** 是否允许新预约；null 表示数据尚未补齐。 */
    private Boolean isEnabled;

    /** Firebase 反序列化所需。 */
    public RestaurantTable() {}

    public RestaurantTable(String tableNumber, int capacity, String occupancyStatus) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.occupancyStatus = occupancyStatus;
    }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getOccupancyStatus() { return occupancyStatus; }
    public void setOccupancyStatus(String occupancyStatus) { this.occupancyStatus = occupancyStatus; }
    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean enabled) { isEnabled = enabled; }
}
