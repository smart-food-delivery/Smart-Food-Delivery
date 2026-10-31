package com.smartfooddelivery.model;

import com.smartfooddelivery.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDeliveryDetailsTest {

    private Order createOrder() {
        User customer = new User(1, "customer", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        Order order = new Order(
                10,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        order.setDeliveryDetails(5.0, 10.0);
        return order;
    }

    private void assertOriginalDetailsUnchanged(Order order) {
        assertEquals(5.0, order.getDeliveryDistanceKm(), 0.001);
        assertEquals(10.0, order.getDeliveryFee(), 0.001);
        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void rejectsInvalidDistanceWithoutChangingDetails() {
        Order order = createOrder();

        double[] invalidValues = {
                -1.0,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double distance : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> order.setDeliveryDetails(distance, 12.0));

            assertOriginalDetailsUnchanged(order);
        }
    }

    @Test
    void rejectsInvalidFeeWithoutChangingDetails() {
        Order order = createOrder();

        double[] invalidValues = {
                -1.0,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double fee : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> order.setDeliveryDetails(6.0, fee));

            assertOriginalDetailsUnchanged(order);
        }
    }

    @Test
    void acceptsZeroDistanceAndFreeDelivery() {
        Order order = createOrder();

        order.setDeliveryDetails(0.0, 0.0);

        assertEquals(0.0, order.getDeliveryDistanceKm(), 0.001);
        assertEquals(0.0, order.getDeliveryFee(), 0.001);
        assertEquals(50.0, order.getTotal(), 0.001);
    }

    @Test
    void rejectsChangesAfterConfirmation() {
        Order order = createOrder();
        order.changeStatus(OrderStatus.CONFIRMED);

        assertThrows(IllegalStateException.class,
                () -> order.setDeliveryDetails(6.0, 12.0));

        assertOriginalDetailsUnchanged(order);
    }

    @Test
    void rejectsChangesAfterCancellation() {
        Order order = createOrder();
        order.cancel();

        assertThrows(IllegalStateException.class,
                () -> order.setDeliveryDetails(6.0, 12.0));

        assertOriginalDetailsUnchanged(order);
    }

    @Test
    void rejectsChangesAfterPaymentAndDelivery() {
        Order order = createOrder();
        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.PREPARING);
        order.changeStatus(OrderStatus.READY);
        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.markAsPaid();

        assertThrows(IllegalStateException.class,
                () -> order.setDeliveryDetails(6.0, 12.0));

        assertOriginalDetailsUnchanged(order);

        order.changeStatus(OrderStatus.DELIVERED);

        assertThrows(IllegalStateException.class,
                () -> order.setDeliveryDetails(6.0, 12.0));

        assertOriginalDetailsUnchanged(order);
    }
}