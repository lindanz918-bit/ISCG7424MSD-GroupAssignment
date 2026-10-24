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

    static Map<String, Object> cancel(String id, String uid, Map<?, ?> order, String slotOwner) {
        String date = text(order.get("bookingDate"));
        String table = text(order.get("tableId"));
        if (!validKey(id) || !validKey(table) || !validKey(uid)
                || !uid.equals(order.get("customerId")) || !"PENDING".equals(order.get("status"))
                || !date.equals(canonicalDate(date)) || !id.equals(slotOwner)) {
            throw new IllegalArgumentException("Only your pending, migrated booking can be cancelled.");
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("Orders/" + id + "/status", "CANCELLED");
        updates.put("Orders/" + id + "/updatedAt", ServerValue.TIMESTAMP);
        updates.put("tableReservations/" + date + "/" + table, null);
        return updates;
    }
}
