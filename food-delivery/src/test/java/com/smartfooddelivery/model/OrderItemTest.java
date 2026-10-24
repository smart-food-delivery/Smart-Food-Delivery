package com.smartfooddelivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderItemTest {

    @Test
    void calculatesSubtotalCorrectly() {
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        OrderItem item = new OrderItem(pizza, 2);

        assertEquals(50.0, item.getSubtotal(), 0.001);
    }

    @Test
    void rejectsZeroQuantity() {
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(pizza, 0));
    }

    @Test
    void rejectsNegativeQuantity() {
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(pizza, -2));
    }

    @Test
    void rejectsMissingMenuItem() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(null, 1));
    }
}