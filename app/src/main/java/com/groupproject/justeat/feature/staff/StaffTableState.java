package com.groupproject.justeat.feature.staff;

import com.google.firebase.database.DataSnapshot;

/** Staff 专用读取模型，避免更改其他角色使用的 RestaurantTable。
 * key 是数据库身份；number 只是展示编号。缺少新字段时只读，不能猜测旧 RESERVED 的含义。
 */
public final class StaffTableState {
    public final String key, number, occupancy, legacyStatus;
    public final int capacity;
    public final Boolean enabled;
    public StaffTableState(DataSnapshot data) {
        key = data.getKey();
        number = text(data, "tableNumber");
        Object count = data.child("capacity").getValue();
        capacity = count instanceof Number ? ((Number) count).intValue() : 0;
        Object flag = data.child("isEnabled").getValue();
        enabled = flag instanceof Boolean ? (Boolean) flag : null;
        occupancy = text(data, "occupancyStatus");
        legacyStatus = text(data, "status");
    }
    public boolean isMigrated() {
        return enabled != null && ("AVAILABLE".equals(occupancy) || "OCCUPIED".equals(occupancy));
    }
    public static String text(DataSnapshot data, String field) {
        Object value = data.child(field).getValue();
        return value == null ? "" : value.toString();
    }
}
