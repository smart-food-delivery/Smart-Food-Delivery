package com.smartfooddelivery.model;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.domain.PaymentStatus;
import com.smartfooddelivery.strategy.PromotionalPricingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StandardOrderTest {

    private final User customer =
            new User(1, "customer", "hash", "Customer");

    private final Restaurant restaurant =
            new Restaurant(1, "Pizza House", "Nablus", true);

    private List<OrderItem> validItems() {
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);
        return List.of(new OrderItem(pizza, 2));
    }

    private StandardOrder createOrder() {
        return new StandardOrder(
                10,
                customer,
                restaurant,
                validItems(),
                "Nablus, Rafidia, Building 10"
        );
    }

    @Test
    void startsPendingAndUnpaid() {
        Order order = createOrder();

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
    }

    @Test
    void addsDeliveryWithoutExtraOrderTypeFee() {
        Order order = createOrder();
        order.setDeliveryDetails(5.0, 10.0);

        assertEquals(50.0, order.getSubtotal(), 0.001);
        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void supportsExistingPricingStrategies() {
        Order order = createOrder();
        order.setDeliveryDetails(5.0, 10.0);
        order.setPricingStrategy(new PromotionalPricingStrategy(10.0));

        assertEquals(55.0, order.getTotal(), 0.001);
    }

    @Test
    void rejectsEmptyItems() {
        assertThrows(IllegalArgumentException.class,
                () -> new StandardOrder(
                        10,
                        customer,
                        restaurant,
                        List.of(),
                        "Nablus, Rafidia, Building 10"
                ));
    }
}