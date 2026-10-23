package com.groupproject.justeat.feature.staff;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/** 一桌一天的纯业务规则；数据库日期固定为餐厅当地日历日期，而不是 UTC 时间。 */
public final class StaffBookingPolicy {
    private StaffBookingPolicy() {}
    public static boolean validKey(String value) {
        if (value == null || value.isEmpty()) return false;
        for (char c : value.toCharArray())
            if (".#$[]/".indexOf(c) >= 0 || Character.isISOControl(c)) return false;
        return true;
    }
    public static boolean validDate(String value) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        return format.parse(value, position) != null && position.getIndex() == value.length();
    }
    public static boolean holdsDay(String status) {
        return "PENDING".equals(status) || StaffOrderActionPolicy.isActive(status) || "COMPLETED".equals(status);
    }
    public static boolean releasesDay(String status) {
        return "REJECTED".equals(status) || "CANCELLED".equals(status);
    }
}
