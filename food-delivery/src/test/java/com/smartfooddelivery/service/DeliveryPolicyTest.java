package com.smartfooddelivery.service;

import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.User;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryPolicyTest {

    private Order createOrder(double price) {
        User customer = new User(1, "customer1", "hash", "Customer");

        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        MenuItem item = new MenuItem(1, "Meal", price, 1);

        return new Order(
                1, customer, restaurant,
                List.of(new OrderItem(item, 1)),
                "Nablus, Rafidia, Building 10"
        );
    }

    @Test
    void rejectsOrderBelowMinimum() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));

        Order order = createOrder(29.99);

        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(order));
    }

    @Test
    void acceptsOrderExactlyAtMinimum() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));

        Order order = createOrder(30.0);

        assertDoesNotThrow(() -> policy.validate(order));
    }

    @Test
    void acceptsOrderAboveMinimum() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));

        Order order = createOrder(30.01);

        assertDoesNotThrow(() -> policy.validate(order));
    }

    @Test
    void rejectsNegativeMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryPolicy(
                        -1.0, Set.of("Nablus")
                ));
    }

    @Test
    void rejectsNonFiniteMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryPolicy(
                        Double.NaN, Set.of("Nablus")
                ));

        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryPolicy(
                        Double.POSITIVE_INFINITY, Set.of("Nablus")
                ));

        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryPolicy(
                        Double.NEGATIVE_INFINITY, Set.of("Nablus")
                ));
    }

    @Test
    void rejectsMissingOrder() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));

        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(null));
    }

    @Test
    void rejectsNonFiniteSubtotal() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));

        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(createOrder(Double.NaN)));

        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(
                        createOrder(Double.POSITIVE_INFINITY)
                ));
    }
    @Test
void rejectsRestaurantOutsideDeliveryZone() {
    DeliveryPolicy policy =
            new DeliveryPolicy(30.0, Set.of("Ramallah"));

    // المطعم الموجود في createOrder بمدينة نابلس
    Order order = createOrder(50.0);

    assertThrows(IllegalArgumentException.class,
            () -> policy.validate(order));
}

@Test
void acceptsCityWithDifferentCaseAndExtraSpaces() {
    DeliveryPolicy policy =
            new DeliveryPolicy(30.0, Set.of("  nAbLuS  "));

    Order order = createOrder(50.0);

    assertDoesNotThrow(() -> policy.validate(order));
}
    private Order createOrderWithAddress(String address) {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem item = new MenuItem(1, "Meal", 50.0, 1);

        return new Order(
                1, customer, restaurant,
                List.of(new OrderItem(item, 1)),
                address
        );
    }
    @Test
    void rejectsAddressWithoutBuilding() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));
        Order order = createOrderWithAddress("Nablus, Rafidia");

        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(order));
    }

    @Test
    void rejectsAddressWithBlankArea() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));
        Order order = createOrderWithAddress("Nablus,   , Building 10");

        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(order));
    }

    @Test
    void rejectsAddressWithBlankCity() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));
        Order order = createOrderWithAddress(", Rafidia, Building 10");

        assertThrows(IllegalArgumentException.class,
                () -> policy.validate(order));
    }

    @Test
    void acceptsAddressWithArabicCommas() {
        DeliveryPolicy policy =
                new DeliveryPolicy(30.0, Set.of("Nablus"));
        Order order = createOrderWithAddress(
                "Nablus، Rafidia، Building 10"
        );

        assertDoesNotThrow(() -> policy.validate(order));
    }
}