package com.smartfooddelivery.model;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.strategy.NormalPricingStrategy;
import com.smartfooddelivery.strategy.PeakHourPricingStrategy;
import com.smartfooddelivery.strategy.PromotionalPricingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderPricingTest {

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

    @Test
    void usesNormalPricingByDefault() {
        Order order = createOrder();

        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void appliesPromotionalPricing() {
        Order order = createOrder();
        order.setPricingStrategy(new PromotionalPricingStrategy(10.0));

        assertEquals(55.0, order.getTotal(), 0.001);
        assertEquals(50.0, order.getSubtotal(), 0.001);
        assertEquals(10.0, order.getDeliveryFee(), 0.001);
    }

    @Test
    void appliesPeakHourPricing() {
        Order order = createOrder();
        order.setPricingStrategy(new PeakHourPricingStrategy(20.0));

        assertEquals(62.0, order.getTotal(), 0.001);
    }

    @Test
    void canSwitchBackToNormalPricingWhilePending() {
        Order order = createOrder();
        order.setPricingStrategy(new PromotionalPricingStrategy(10.0));
        order.setPricingStrategy(new NormalPricingStrategy());

        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void rejectsMissingStrategyWithoutChangingTotal() {
        Order order = createOrder();

        assertThrows(IllegalArgumentException.class,
                () -> order.setPricingStrategy(null));

        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void rejectsPricingChangesAfterConfirmation() {
        Order order = createOrder();
        order.changeStatus(OrderStatus.CONFIRMED);

        assertThrows(IllegalStateException.class,
                () -> order.setPricingStrategy(
                        new PromotionalPricingStrategy(10.0)
                ));

        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void rejectsPricingChangesAfterCancellation() {
        Order order = createOrder();
        order.cancel();

        assertThrows(IllegalStateException.class,
                () -> order.setPricingStrategy(
                        new PromotionalPricingStrategy(10.0)
                ));
    }

    @Test
    void rejectsPricingChangesAfterDelivery() {
        Order order = createOrder();
        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.PREPARING);
        order.changeStatus(OrderStatus.READY);
        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.changeStatus(OrderStatus.DELIVERED);

        assertThrows(IllegalStateException.class,
                () -> order.setPricingStrategy(
                        new PromotionalPricingStrategy(10.0)
                ));

        assertEquals(60.0, order.getTotal(), 0.001);
    }
}