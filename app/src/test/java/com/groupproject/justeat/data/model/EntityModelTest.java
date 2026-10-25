package com.groupproject.justeat.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EntityModelTest {

    @Test
    public void userSupportsFirebaseConstructionAndPropertyUpdates() {
        User user = new User("u1", "Chris", "Hawkes", "chris@example.com",
                "021000000", "avatar-a", "CUSTOMER", "ACTIVE", 10L, 20L);
        assertEquals("u1", user.getUserId());
        assertEquals("Chris", user.getFirstName());
        assertEquals("Hawkes", user.getLastName());
        assertEquals("chris@example.com", user.getEmail());
        assertEquals("021000000", user.getPhone());
        assertEquals("avatar-a", user.getAvatarUrl());
        assertEquals("CUSTOMER", user.getRole());
        assertEquals("ACTIVE", user.getStatus());
        assertEquals(10L, user.getCreatedAt());
        assertEquals(20L, user.getUpdatedAt());

        user.setUserId("u2");
        user.setFirstName("Alex");
        user.setLastName("Chen");
        user.setEmail("alex@example.com");
        user.setPhone("022000000");
        user.setAvatarUrl("avatar-b");
        user.setRole("STAFF");
        user.setStatus("SUSPENDED");
        user.setCreatedAt(30L);
        user.setUpdatedAt(40L);
        assertEquals("u2", user.getUserId());
        assertEquals("Alex", user.getFirstName());
        assertEquals("Chen", user.getLastName());
        assertEquals("alex@example.com", user.getEmail());
        assertEquals("022000000", user.getPhone());
        assertEquals("avatar-b", user.getAvatarUrl());
        assertEquals("STAFF", user.getRole());
        assertEquals("SUSPENDED", user.getStatus());
        assertEquals(30L, user.getCreatedAt());
        assertEquals(40L, user.getUpdatedAt());
    }

    @Test
    public void menuCategorySupportsFirebaseConstructionAndPropertyUpdates() {
        MenuCategory category = new MenuCategory("c1", "Mains", "Main meals", 1,
                true, 10L, 20L);
        assertEquals("c1", category.getCategoryId());
        assertEquals("Mains", category.getName());
        assertEquals("Main meals", category.getDescription());
        assertEquals(1, category.getDisplayOrder());
        assertTrue(category.isActive());
        assertEquals(10L, category.getCreatedAt());
        assertEquals(20L, category.getUpdatedAt());

        category.setCategoryId("c2");
        category.setName("Desserts");
        category.setDescription("Sweet meals");
        category.setDisplayOrder(2);
        category.setActive(false);
        category.setCreatedAt(30L);
        category.setUpdatedAt(40L);
        assertEquals("c2", category.getCategoryId());
        assertEquals("Desserts", category.getName());
        assertEquals("Sweet meals", category.getDescription());
        assertEquals(2, category.getDisplayOrder());
        assertFalse(category.isActive());
        assertEquals(30L, category.getCreatedAt());
        assertEquals(40L, category.getUpdatedAt());
    }

    @Test
    public void menuItemSupportsFirebaseConstructionAndPropertyUpdates() {
        MenuItem item = new MenuItem("m1", "Noodles", "image-a", "Mild", "12.99", "Spicy noodles");
        assertEquals("m1", item.getMenuItemId());

        assertEquals("Noodles", item.getmealName());
        assertEquals("Spicy noodles", item.getDescription());
        assertEquals("image-a", item.getImageUrl());


        item.setMenuItemId("m2");

        item.setmealName("Cake");
        item.setDescription("Chocolate cake");
        item.setImageUrl("image-b");

        assertEquals("m2", item.getMenuItemId());

        assertEquals("Cake", item.getmealName());
        assertEquals("Chocolate cake", item.getDescription());
        assertEquals("image-b", item.getImageUrl());

    }

//    @Test
//    public void restaurantTableSupportsFirebaseConstructionAndPropertyUpdates() {
//
//        RestaurantTable table = new RestaurantTable("t1", "01", 4, "Window",
//                "AVAILABLE", 10L, 20L);
//        assertEquals("t1", table.getTableId());
//        assertEquals("01", table.getTableNumber());
//        assertEquals(4, table.getCapacity());
//        assertEquals("Window", table.getLocation());
//        assertEquals("AVAILABLE", table.getStatus());
//        assertEquals(10L, table.getCreatedAt());
//        assertEquals(20L, table.getUpdatedAt());
//
//        table.setTableId("t2");
//        table.setTableNumber("02");
//        table.setCapacity(6);
//        table.setLocation("Patio");
//        table.setStatus("RESERVED");
//        table.setCreatedAt(30L);
//        table.setUpdatedAt(40L);
//        assertEquals("t2", table.getTableId());
//        assertEquals("02", table.getTableNumber());
//        assertEquals(6, table.getCapacity());
//        assertEquals("Patio", table.getLocation());
//        assertEquals("RESERVED", table.getStatus());
//        assertEquals(30L, table.getCreatedAt());
//        assertEquals(40L, table.getUpdatedAt());
//    }

    @Test
    public void orderItemSupportsFirebaseConstructionAndPropertyUpdates() {
        OrderItem item = new OrderItem("oi1", "o1", "m1", "Noodles",
                3599L, 2, 7198L, "Less salt", "PENDING", 10L, 20L);
        assertEquals("oi1", item.getOrderItemId());
        assertEquals("o1", item.getOrderId());
        assertEquals("m1", item.getMenuItemId());
        assertEquals("Noodles", item.getMenuItemName());
        assertEquals(3599L, item.getUnitPriceInCents());
        assertEquals(2, item.getQuantity());
        assertEquals(7198L, item.getSubtotalInCents());
        assertEquals("Less salt", item.getNotes());
        assertEquals("PENDING", item.getStatus());
        assertEquals(10L, item.getCreatedAt());
        assertEquals(20L, item.getUpdatedAt());

        item.setOrderItemId("oi2");
        item.setOrderId("o2");
        item.setMenuItemId("m2");
        item.setMenuItemName("Cake");
        item.setUnitPriceInCents(1299L);
        item.setQuantity(1);
        item.setSubtotalInCents(1299L);
        item.setNotes("One candle");
        item.setStatus("SERVED");
        item.setCreatedAt(30L);
        item.setUpdatedAt(40L);
        assertEquals("oi2", item.getOrderItemId());
        assertEquals("o2", item.getOrderId());
        assertEquals("m2", item.getMenuItemId());
        assertEquals("Cake", item.getMenuItemName());
        assertEquals(1299L, item.getUnitPriceInCents());
        assertEquals(1, item.getQuantity());
        assertEquals(1299L, item.getSubtotalInCents());
        assertEquals("One candle", item.getNotes());
        assertEquals("SERVED", item.getStatus());
        assertEquals(30L, item.getCreatedAt());
        assertEquals(40L, item.getUpdatedAt());
    }
}
