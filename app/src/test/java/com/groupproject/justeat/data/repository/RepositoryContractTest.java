package com.groupproject.justeat.data.repository;

import static org.junit.Assert.assertEquals;

import com.groupproject.justeat.data.model.Booking;
import com.groupproject.justeat.data.model.MenuItem;
import com.groupproject.justeat.data.model.Order;
import com.groupproject.justeat.data.model.RestaurantTable;

import org.junit.Test;

import java.lang.reflect.Method;

public class RepositoryContractTest {

    @Test
    public void callbackExposesSuccessAndErrorResults() throws Exception {
        assertReturnType(void.class,
                RepositoryCallback.class.getMethod("onSuccess", Object.class));
        assertReturnType(void.class,
                RepositoryCallback.class.getMethod("onError", String.class));
    }

    @Test
    public void authenticationBoundaryDoesNotExposeFirebaseTypes() throws Exception {
        assertReturnType(void.class, AuthRepository.class.getMethod(
                "signIn", String.class, String.class, RepositoryCallback.class));
        assertReturnType(void.class, AuthRepository.class.getMethod(
                "signOut", RepositoryCallback.class));
        assertReturnType(String.class, AuthRepository.class.getMethod("getCurrentUserId"));
        assertReturnType(boolean.class, AuthRepository.class.getMethod("isSignedIn"));
    }

    @Test
    public void menuBoundarySupportsLoadSaveAndDelete() throws Exception {
        assertReturnType(void.class, MenuRepository.class.getMethod(
                "getMenuItems", RepositoryCallback.class));
        assertReturnType(void.class, MenuRepository.class.getMethod(
                "saveMenuItem", MenuItem.class, RepositoryCallback.class));
        assertReturnType(void.class, MenuRepository.class.getMethod(
                "deleteMenuItem", String.class, RepositoryCallback.class));
    }

    @Test
    public void tableBoundarySupportsLoadSaveAndDelete() throws Exception {
        assertReturnType(void.class, TableRepository.class.getMethod(
                "getTables", RepositoryCallback.class));
        assertReturnType(void.class, TableRepository.class.getMethod(
                "saveTable", RestaurantTable.class, RepositoryCallback.class));
        assertReturnType(void.class, TableRepository.class.getMethod(
                "deleteTable", String.class, RepositoryCallback.class));
    }

    @Test
    public void bookingBoundarySupportsCustomerWorkflow() throws Exception {
        assertReturnType(void.class, BookingRepository.class.getMethod(
                "getBookingsForCustomer", String.class, RepositoryCallback.class));
        assertReturnType(void.class, BookingRepository.class.getMethod(
                "saveBooking", Booking.class, RepositoryCallback.class));
        assertReturnType(void.class, BookingRepository.class.getMethod(
                "updateBookingStatus", String.class, String.class, RepositoryCallback.class));
        assertReturnType(void.class, BookingRepository.class.getMethod(
                "cancelBooking", String.class, RepositoryCallback.class));
    }

    @Test
    public void orderBoundarySupportsCustomerAndStaffWorkflow() throws Exception {
        assertReturnType(void.class, OrderRepository.class.getMethod(
                "getOrdersForCustomer", String.class, RepositoryCallback.class));
        assertReturnType(void.class, OrderRepository.class.getMethod(
                "getOrdersByStatus", String.class, RepositoryCallback.class));
        assertReturnType(void.class, OrderRepository.class.getMethod(
                "saveOrder", Order.class, RepositoryCallback.class));
        assertReturnType(void.class, OrderRepository.class.getMethod(
                "updateOrderStatus", String.class, String.class, RepositoryCallback.class));
    }

    private static void assertReturnType(Class<?> expected, Method method) {
        assertEquals(expected, method.getReturnType());
    }
}
