package com.smartfooddelivery.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromotionalPricingStrategyTest {

    @Test
    void discountsFoodWithoutDiscountingDelivery() {
        PricingStrategy strategy = new PromotionalPricingStrategy(10.0);

        assertEquals(51.0,
                strategy.calculateTotal(50.0, 6.0), 0.001);
    }

    @Test
    void acceptsZeroDiscount() {
        PricingStrategy strategy = new PromotionalPricingStrategy(0.0);

        assertEquals(56.0,
                strategy.calculateTotal(50.0, 6.0), 0.001);
    }

    @Test
    void fullDiscountLeavesOnlyDeliveryFee() {
        PricingStrategy strategy = new PromotionalPricingStrategy(100.0);

        assertEquals(6.0,
                strategy.calculateTotal(50.0, 6.0), 0.001);
    }

    @Test
    void roundsAfterApplyingDiscountAndAddingDelivery() {
        PricingStrategy strategy = new PromotionalPricingStrategy(15.0);

        assertEquals(19.99,
                strategy.calculateTotal(19.99, 3.0), 0.001);
    }

    @Test
    void rejectsInvalidDiscountPercentages() {
        double[] invalidValues = {
                -1.0, 100.01, Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double value : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> new PromotionalPricingStrategy(value));
        }
    }

    @Test
    void rejectsInvalidSubtotal() {
        PricingStrategy strategy = new PromotionalPricingStrategy(10.0);
        double[] invalidValues = {
                -1.0, Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double value : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateTotal(value, 6.0));
        }
    }

    @Test
    void rejectsInvalidDeliveryFee() {
        PricingStrategy strategy = new PromotionalPricingStrategy(10.0);
        double[] invalidValues = {
                -1.0, Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double value : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateTotal(50.0, value));
        }
    }

    @Test
    void rejectsOverflowingTotal() {
        PricingStrategy strategy = new PromotionalPricingStrategy(0.0);

        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculateTotal(
                        Double.MAX_VALUE, Double.MAX_VALUE
                ));
    }
}