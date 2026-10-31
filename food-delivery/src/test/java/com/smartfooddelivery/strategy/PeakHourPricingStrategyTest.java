package com.smartfooddelivery.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakHourPricingStrategyTest {

    @Test
    void appliesSurchargeOnlyToDelivery() {
        PricingStrategy strategy = new PeakHourPricingStrategy(20.0);

        assertEquals(62.0,
                strategy.calculateTotal(50.0, 10.0), 0.001);
    }

    @Test
    void acceptsZeroSurcharge() {
        PricingStrategy strategy = new PeakHourPricingStrategy(0.0);

        assertEquals(60.0,
                strategy.calculateTotal(50.0, 10.0), 0.001);
    }

    @Test
    void keepsFreeDeliveryFree() {
        PricingStrategy strategy = new PeakHourPricingStrategy(20.0);

        assertEquals(50.0,
                strategy.calculateTotal(50.0, 0.0), 0.001);
    }

    @Test
    void roundsFinalTotalToTwoDecimalPlaces() {
        PricingStrategy strategy = new PeakHourPricingStrategy(15.0);

        assertEquals(53.44,
                strategy.calculateTotal(50.0, 2.99), 0.001);
    }

    @Test
    void rejectsInvalidSurcharge() {
        double[] invalidValues = {
                -1.0, Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double value : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> new PeakHourPricingStrategy(value));
        }
    }

    @Test
    void rejectsInvalidSubtotal() {
        PricingStrategy strategy = new PeakHourPricingStrategy(20.0);
        double[] invalidValues = {
                -1.0, Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double value : invalidValues) {
            assertThrows(IllegalArgumentException.class,
                    () -> strategy.calculateTotal(value, 10.0));
        }
    }

    @Test
    void rejectsInvalidDeliveryFee() {
        PricingStrategy strategy = new PeakHourPricingStrategy(20.0);
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
        PricingStrategy strategy = new PeakHourPricingStrategy(100.0);

        assertThrows(IllegalArgumentException.class,
                () -> strategy.calculateTotal(50.0, Double.MAX_VALUE));
    }
}