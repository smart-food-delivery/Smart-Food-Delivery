package com.smartfooddelivery.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderStatusTest {

    @Test
    void pendingOrderCanBeCancelled() {
        assertTrue(OrderStatus.PENDING.canBeCancelled());
    }
    @Test
    void preparingOrderCannotBeCancelled() {
        assertFalse(OrderStatus.PREPARING.canBeCancelled());
    }
    @Test
    void readyOrderCannotBeCancelled() {
        assertFalse(OrderStatus.READY.canBeCancelled());
    }

    @Test
    void outForDeliveryOrderCannotBeCancelled() {
        assertFalse(OrderStatus.OUT_FOR_DELIVERY.canBeCancelled());
    }

    @Test
    void deliveredOrderCannotBeCancelled() {
        assertFalse(OrderStatus.DELIVERED.canBeCancelled());
    }

    @Test
    void cancelledOrderCannotBeCancelledAgain() {
        assertFalse(OrderStatus.CANCELLED.canBeCancelled());
    }
}