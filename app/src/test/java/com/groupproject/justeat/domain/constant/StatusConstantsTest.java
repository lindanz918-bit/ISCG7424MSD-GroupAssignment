package com.groupproject.justeat.domain.constant;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class StatusConstantsTest {

    @Test
    public void userRolesMatchPersistedFirebaseValues() {
        assertArrayEquals(
                new String[]{"CUSTOMER", "STAFF", "MANAGER"},
                new String[]{UserRole.CUSTOMER, UserRole.STAFF, UserRole.MANAGER}
        );
    }

    @Test
    public void userStatusesMatchPersistedFirebaseValues() {
        assertArrayEquals(
                new String[]{"ACTIVE", "SUSPENDED", "DISABLED"},
                new String[]{UserStatus.ACTIVE, UserStatus.SUSPENDED, UserStatus.DISABLED}
        );
    }

    @Test
    public void tableStatusesMatchPersistedFirebaseValues() {
        assertArrayEquals(
                new String[]{"AVAILABLE", "OCCUPIED", "RESERVED", "UNAVAILABLE"},
                new String[]{TableStatus.AVAILABLE, TableStatus.OCCUPIED,
                        TableStatus.RESERVED, TableStatus.UNAVAILABLE}
        );
    }

    @Test
    public void bookingStatusesMatchPersistedFirebaseValues() {
        assertArrayEquals(
                new String[]{"PENDING", "CONFIRMED", "REJECTED", "CANCELLED", "COMPLETED"},
                new String[]{BookingStatus.PENDING, BookingStatus.CONFIRMED,
                        BookingStatus.REJECTED, BookingStatus.CANCELLED, BookingStatus.COMPLETED}
        );
    }

    @Test
    public void orderStatusesMatchPersistedFirebaseValues() {
        assertArrayEquals(
                new String[]{"PENDING", "ACCEPTED", "REJECTED", "PREPARING",
                        "READY", "COMPLETED", "CANCELLED"},
                new String[]{OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.REJECTED,
                        OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.COMPLETED,
                        OrderStatus.CANCELLED}
        );
    }

    @Test
    public void orderItemStatusesMatchPersistedFirebaseValues() {
        assertArrayEquals(
                new String[]{"PENDING", "PREPARING", "SERVED", "CANCELLED"},
                new String[]{OrderItemStatus.PENDING, OrderItemStatus.PREPARING,
                        OrderItemStatus.SERVED, OrderItemStatus.CANCELLED}
        );
    }
}

