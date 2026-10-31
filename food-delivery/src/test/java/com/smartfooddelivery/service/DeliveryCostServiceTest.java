
package com.smartfooddelivery.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryCostServiceTest {

    @Test
    void calculatesDeliveryFeeBasedOnDistance() {
        DeliveryCostService service = new DeliveryCostService(2.0);

        double fee = service.calculateDeliveryFee(5.0);

        assertEquals(10.0, fee, 0.001);
    }

    @Test
    void calculatesZeroFeeForZeroDistance() {
        DeliveryCostService service = new DeliveryCostService(2.0);

        assertEquals(0.0, service.calculateDeliveryFee(0.0));
    }

    @Test
    void roundsDeliveryFeeToTwoDecimalPlaces() {
        DeliveryCostService service = new DeliveryCostService(1.75);

        assertEquals(5.25, service.calculateDeliveryFee(3.0), 0.001);
    }

    @Test
    void rejectsNegativeDistance() {
        DeliveryCostService service = new DeliveryCostService(2.0);

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateDeliveryFee(-1.0));
    }

    @Test
    void rejectsInvalidDistance() {
        DeliveryCostService service = new DeliveryCostService(2.0);

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateDeliveryFee(Double.NaN));

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateDeliveryFee(Double.POSITIVE_INFINITY));
    }

    @Test
    void rejectsInvalidPricePerKm() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryCostService(-2.0));

        assertThrows(IllegalArgumentException.class,
                () -> new DeliveryCostService(Double.NaN));
    }
}
