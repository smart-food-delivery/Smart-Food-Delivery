package com.smartfooddelivery.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalPricingStrategyTest {

    private final PricingStrategy strategy = new NormalPricingStrategy();

    @Test
    void addsDeliveryFeeToSubtotal() {
        assertEquals(56.0, strategy.calculateTotal(50.0, 6.0), 0.001);
    }

    @Test
    void acceptsFreeDelivery() {
        assertEquals(50.0, strategy.calculateTotal(50.0, 0.0), 0.001);
    }

    @Test
    void roundsTotalToTwoDecimalPlaces() {
        assertEquals(12.35, strategy.calculateTotal(10.125, 2.22), 0.001);
    }

    @Test
    void rejectsInvalidSubtotal() {
        double[] invalidValues = {
                -1.0,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double value : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateTotal(value, 5.0));
        }
    }

    @Test
    void rejectsInvalidDeliveryFee() {
        double[] invalidValues = {
                -1.0,
                Double.NaN,
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
        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculateTotal(
                        Double.MAX_VALUE, Double.MAX_VALUE
                ));
    }
}