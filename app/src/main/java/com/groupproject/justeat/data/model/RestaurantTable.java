package com.groupproject.justeat.data.model;

/** 餐厅餐桌实体，用于容量设置、位置说明和可用状态管理。 */
public class RestaurantTable {
    /** 餐桌唯一 ID。 */
    private String tableId;
    /** 显示给用户的桌号。 */
     private String tableNumber;
    /** 最大容纳人数。 */
    private int capacity;
    /** 餐桌所在区域，例如 Window 或 Patio。 */
    private String location;

    /** 餐桌状态，值来自 TableStatus。 */
    private String status;

/** 创建时间，Unix 毫秒时间戳。 */
//    private long createdAt;
//    /** 最后更新时间，Unix 毫秒时间戳。 */
//    private long updatedAt;

    /** Firebase 反序列化需要的公开无参构造方法。 */
    public RestaurantTable() {
    }

    /** 创建包含全部餐桌字段的实体。 */
    public RestaurantTable(
                            String tableId,

                           String tableNumber,
                           int capacity,
<<<<<<< HEAD
                           String location,
=======
//                           String location,
>>>>>>> origin/main
                           String status
//                           long createdAt,
//                           long updatedAt
    ) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
<<<<<<< HEAD
        this.location = location;
=======
//        this.location = location;
>>>>>>> origin/main
        this.status = status;
//        this.createdAt = createdAt;
//        this.updatedAt = updatedAt;
    }
    public RestaurantTable(String tableNumber, int capacity, String status) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

<<<<<<< HEAD
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

=======
//    public String getLocation() {
//        return location;
//    }
//
//    public void setLocation(String location) {
//        this.location = location;
//    }
//
>>>>>>> origin/main
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
<<<<<<< HEAD

=======
//
>>>>>>> origin/main
//    public long getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(long createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public long getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(long updatedAt) {
//        this.updatedAt = updatedAt;
//    }
}

