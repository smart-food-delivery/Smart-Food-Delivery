package com.smartfooddelivery.model;

import com.smartfooddelivery.strategy.PeakHourPricingStrategy;
import com.smartfooddelivery.strategy.PromotionalPricingStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpressOrderTest {

    private ExpressOrder createOrder(double expressFee) {
        User customer = new User(1, "customer", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        ExpressOrder order = new ExpressOrder(
                10,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10",
                expressFee
        );

        order.setDeliveryDetails(5.0, 10.0);
        return order;
    }

    @Test
    void addsExpressFeeThroughOrderReference() {
        Order order = createOrder(5.0);

        assertEquals(65.0, order.getTotal(), 0.001);
        assertEquals(50.0, order.getSubtotal(), 0.001);
        assertEquals(10.0, order.getDeliveryFee(), 0.001);
    }

    @Test
    void storesConfiguredExpressFee() {
        ExpressOrder order = createOrder(7.50);

        assertEquals(7.50, order.getExpressFee(), 0.001);
        assertEquals(67.50, order.getTotal(), 0.001);
    }

    @Test
    void keepsExpressFeeWhenApplyingFoodDiscount() {
        Order order = createOrder(5.0);
        order.setPricingStrategy(new PromotionalPricingStrategy(10.0));

        // Food after discount: 45, delivery: 10, express: 5
        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void appliesPeakSurchargeOnlyToBaseDeliveryFee() {
        Order order = createOrder(5.0);
        order.setPricingStrategy(new PeakHourPricingStrategy(20.0));

        // Food: 50, delivery after surcharge: 12, express: 5
        assertEquals(67.0, order.getTotal(), 0.001);
    }

    @Test
    void repeatedTotalCalculationDoesNotAccumulateFees() {
        Order order = createOrder(5.0);

        assertEquals(65.0, order.getTotal(), 0.001);
        assertEquals(65.0, order.getTotal(), 0.001);
        assertEquals(10.0, order.getDeliveryFee(), 0.001);
    }

    @Test
    void rejectsInvalidExpressFees() {
        double[] invalidValues = {
                0.0,
                -1.0,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double fee : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> createOrder(fee));
        }
    }

    @Test
    void rejectsExpressFeeWithFractionOfACent() {
        assertThrows(IllegalArgumentException.class,
                () -> createOrder(5.001));
    }

    @Test
    void rejectsOverflowingTotal() {
        ExpressOrder order = createOrder(Double.MAX_VALUE);
        order.setDeliveryDetails(5.0, Double.MAX_VALUE);

        assertThrows(IllegalArgumentException.class, order::getTotal);
    }
}