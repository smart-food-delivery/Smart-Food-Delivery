package com.smartfooddelivery.model;

import com.smartfooddelivery.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    void newOrderHasPendingStatus() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        Order order = new Order(
                1, customer, restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void calculatesSubtotalForMultipleItems() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);
        MenuItem juice = new MenuItem(2, "Juice", 5.0, 1);

        Order order = new Order(
                1, customer, restaurant,
                List.of(
                        new OrderItem(pizza, 2),
                        new OrderItem(juice, 3)
                ),
                "Nablus, Rafidia, Building 10"
        );

        assertEquals(65.0, order.getSubtotal(), 0.001);
    }

    @Test
    void rejectsEmptyOrder() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        assertThrows(IllegalArgumentException.class,
                () -> new Order(
                        1, customer, restaurant,
                        List.of(),
                        "Nablus, Rafidia, Building 10"
                ));
    }
    @Test
    void rejectsMissingCustomer() {
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        assertThrows(IllegalArgumentException.class,
                () -> new Order(
                        1, null, restaurant,
                        List.of(new OrderItem(pizza, 1)),
                        "Nablus, Rafidia, Building 10"
                ));
    }

    @Test
    void rejectsMissingRestaurant() {
        User customer = new User(1, "customer1", "hash", "Customer");
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        assertThrows(IllegalArgumentException.class,
                () -> new Order(
                        1, customer, null,
                        List.of(new OrderItem(pizza, 1)),
                        "Nablus, Rafidia, Building 10"
                ));
    }

    @Test
    void rejectsMissingItemsList() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        assertThrows(IllegalArgumentException.class,
                () -> new Order(
                        1, customer, restaurant, null,
                        "Nablus, Rafidia, Building 10"
                ));
    }

    @Test
    void rejectsNullItemInsideOrder() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        List<OrderItem> items = new java.util.ArrayList<>();
        items.add(null);

        assertThrows(IllegalArgumentException.class,
                () -> new Order(
                        1, customer, restaurant, items,
                        "Nablus, Rafidia, Building 10"
                ));
    }

    @Test
    void rejectsMissingDeliveryAddress() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        assertThrows(IllegalArgumentException.class,
                () -> new Order(
                        1, customer, restaurant,
                        List.of(new OrderItem(pizza, 1)),
                        null
                ));
    }

    @Test
    void rejectsBlankDeliveryAddress() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        assertThrows(IllegalArgumentException.class,
                () -> new Order(
                        1, customer, restaurant,
                        List.of(new OrderItem(pizza, 1)),
                        "   "
                ));
    }
    @Test
    void cancelsPendingOrder() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        Order order = new Order(
                1, customer, restaurant,
                List.of(new OrderItem(pizza, 1)),
                "Nablus, Rafidia, Building 10"
        );

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void rejectsCancellingOrderTwice() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        Order order = new Order(
                1, customer, restaurant,
                List.of(new OrderItem(pizza, 1)),
                "Nablus, Rafidia, Building 10"
        );

        order.cancel();

        assertThrows(IllegalStateException.class, () -> order.cancel());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }
    @Test
    void rejectsCancellationAfterPreparationStarts() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        Order order = new Order(
                1, customer, restaurant,
                List.of(new OrderItem(pizza, 1)),
                "Nablus, Rafidia, Building 10"
        );

        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.PREPARING);

        assertThrows(IllegalStateException.class, () -> order.cancel());
        assertEquals(OrderStatus.PREPARING, order.getStatus());
    }
    private Order createOrderForStatusTests() {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        return new Order(
                1, customer, restaurant,
                List.of(new OrderItem(pizza, 1)),
                "Nablus, Rafidia, Building 10"
        );
    }

    @Test
    void followsValidLifecycleAndRejectsChangesAfterDelivery() {
        Order order = createOrderForStatusTests();

        OrderStatus[] nextStatuses = {
                OrderStatus.CONFIRMED,
                OrderStatus.PREPARING,
                OrderStatus.READY,
                OrderStatus.OUT_FOR_DELIVERY,
                OrderStatus.DELIVERED
        };

        for (OrderStatus nextStatus : nextStatuses) {
            order.changeStatus(nextStatus);
            assertEquals(nextStatus, order.getStatus());
        }

        assertThrows(IllegalStateException.class,
                () -> order.changeStatus(OrderStatus.READY));

        assertThrows(IllegalStateException.class,
                () -> order.cancel());

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void rejectsSkippingStatuses() {
        Order order = createOrderForStatusTests();

        assertThrows(IllegalStateException.class,
                () -> order.changeStatus(OrderStatus.PREPARING));

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void rejectsMovingBackwards() {
        Order order = createOrderForStatusTests();
        order.changeStatus(OrderStatus.CONFIRMED);

        assertThrows(IllegalStateException.class,
                () -> order.changeStatus(OrderStatus.PENDING));

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void rejectsStatusChangesAfterCancellation() {
        Order order = createOrderForStatusTests();
        order.cancel();

        assertThrows(IllegalStateException.class,
                () -> order.changeStatus(OrderStatus.CONFIRMED));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void rejectsNullStatus() {
        Order order = createOrderForStatusTests();

        assertThrows(IllegalArgumentException.class,
                () -> order.changeStatus(null));

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void rejectsChangingToSameStatus() {
        Order order = createOrderForStatusTests();

        assertThrows(IllegalStateException.class,
                () -> order.changeStatus(OrderStatus.PENDING));

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
}
