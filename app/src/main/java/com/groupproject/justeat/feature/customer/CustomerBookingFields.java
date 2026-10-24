package com.groupproject.justeat.feature.customer;

import com.google.firebase.database.ServerValue;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.data.model.TableReservation;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/** 仅处理预约字段转换，不持有数据库；页面直接调用 Firebase。 */
final class CustomerBookingFields {
    static final TimeZone RESTAURANT_ZONE = TimeZone.getTimeZone("Pacific/Auckland");

    static String canonicalDate(String value) {
        if (value == null) return null;
        String pattern;
        if (value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) pattern = "yyyy-MM-dd";
        else if (value.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) pattern = "dd/MM/yyyy";
        else return null;
        SimpleDateFormat input = format(pattern);
        ParsePosition position = new ParsePosition(0);
        Date parsed = input.parse(value, position);
        return parsed == null || position.getIndex() != value.length()
                ? null : format("yyyy-MM-dd").format(parsed);
    }

    static SimpleDateFormat format(String pattern) {
        SimpleDateFormat result = new SimpleDateFormat(pattern, Locale.UK);
        result.setTimeZone(RESTAURANT_ZONE);
        result.setLenient(false);
        return result;
    }

    static boolean validKey(String key) {
        if (key == null || key.isEmpty()) return false;
        for (char c : key.toCharArray()) {
            if (".#$[]/".indexOf(c) >= 0 || Character.isISOControl(c)) return false;
        }
        return true;
    }

    static boolean available(Object enabled, Object capacity, int guests, boolean reserved) {
        return Boolean.TRUE.equals(enabled) && capacity instanceof Number
                && guests > 0 && ((Number) capacity).doubleValue() >= guests && !reserved;
    }

    static Map<String, Object> create(String id, String uid, String table, String date,
                                       int guests, Object items) {
        if (!validKey(id) || !validKey(uid) || !validKey(table)
                || date == null || !date.equals(canonicalDate(date)) || guests <= 0) {
            throw new IllegalArgumentException("Invalid booking fields");
        }
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", id);
        order.put("customerId", uid);
        order.put("tableId", table);
        order.put("bookingDate", date);
        order.put("guestCount", guests);
        order.put("status", "PENDING");
        order.put("createdAt", ServerValue.TIMESTAMP);
        order.put("updatedAt", ServerValue.TIMESTAMP);
        Map<String, Object> updates = new HashMap<>();
        updates.put("Orders/" + id, order);
        updates.put("tableReservations/" + date + "/" + table, new TableReservation(id));
        if (items != null) updates.put("OrderItems/" + id, items);
        return updates;
    }

    /** 保留 shared Order 的字符串接口，兼容线上旧字符串和新数字人数。 */
    static Order read(String key, Map<?, ?> data) {
        Object created = data.get("createdAt");
        return new Order(key, text(data.get("customerId")), text(data.get("guestCount")),
                text(data.get("bookingDate")), text(data.get("tableId")), text(data.get("status")),
                created instanceof Number ? ((Number) created).longValue() : 0L);
    }

    static String text(Object value) { return value == null ? "" : String.valueOf(value); }

    /** 已结束的订单不能取消，尤其 COMPLETED 必须保留当天名额。 */
    static boolean canCancel(String status) {
        return "PENDING".equals(status) || "ACCEPTED".equals(status)
                || "PREPARING".equals(status) || "READY".equals(status);
    }

    /** 用于订单事务：保留所有旧字段，不迁移日期、桌号或人数，不丢失上菜记录。 */
    static Map<String, Object> cancelledOrder(String id, String uid, Map<?, ?> order) {
        String status = text(order.get("status"));
        if (!validKey(id) || !validKey(uid) || !uid.equals(order.get("customerId"))
                || (!canCancel(status) && !"CANCELLED".equals(status))
                || canonicalDate(text(order.get("bookingDate"))) == null) {
            throw new IllegalArgumentException("This order cannot be cancelled. Check its status and booking date.");
        }
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<?, ?> field : order.entrySet()) result.put((String) field.getKey(), field.getValue());
        if (!"CANCELLED".equals(status)) {
            result.put("status", "CANCELLED");
            result.put("updatedAt", ServerValue.TIMESTAMP);
        }
        return result;
    }

    /** 只按名额所有者判断；旧显示桌号不能用于猜测或删除另一订单的名额。 */
    static boolean ownsSlot(String id, Object slot) {
        return validKey(id) && slot instanceof Map && id.equals(((Map<?, ?>) slot).get("orderId"));
    }
}
