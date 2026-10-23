package com.groupproject.justeat.data.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TableReservationTest {
    @Test
    public void emptyConstructorAndSetterSupportFirebaseMapping() {
        TableReservation reservation = new TableReservation();
        assertNull(reservation.getOrderId());
        reservation.setOrderId("order001");
        assertEquals("order001", reservation.getOrderId());
    }

    @Test
    public void constructorStoresOrderKey() {
        TableReservation reservation = new TableReservation("order002");
        assertEquals("order002", reservation.getOrderId());
    }
}
