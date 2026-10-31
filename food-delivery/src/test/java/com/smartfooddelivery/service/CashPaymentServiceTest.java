package com.smartfooddelivery.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CashPaymentServiceTest {

    private final PaymentService paymentService = new CashPaymentService();

    @Test
    void acceptsExactPayment() {
        assertEquals(0.0,
                paymentService.processPayment(55.0, 55.0), 0.001);
    }

    @Test
    void returnsCorrectChange() {
        assertEquals(5.0,
                paymentService.processPayment(55.0, 60.0), 0.001);
    }

    @Test
    void calculatesChangeForDecimalAmounts() {
        assertEquals(0.01,
                paymentService.processPayment(19.99, 20.0), 0.001);
    }

    @Test
    void rejectsInsufficientCash() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(55.0, 54.99));
    }

    @Test
    void rejectsZeroCashForPositiveAmountDue() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(55.0, 0.0));
    }

    @Test
    void acceptsZeroAmountDueAndZeroCash() {
        assertEquals(0.0,
                paymentService.processPayment(0.0, 0.0), 0.001);
    }

    @Test
    void returnsAllCashWhenNothingIsDue() {
        assertEquals(10.0,
                paymentService.processPayment(0.0, 10.0), 0.001);
    }

    @Test
    void rejectsInvalidAmountDue() {
        double[] invalidAmounts = {
                -1.0,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double amount : invalidAmounts) {
            assertThrows(IllegalArgumentException.class,
                    () -> paymentService.processPayment(amount, 60.0));
        }
    }

    @Test
    void rejectsInvalidAmountReceived() {
        double[] invalidAmounts = {
                -1.0,
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };

        for (double amount : invalidAmounts) {
            assertThrows(IllegalArgumentException.class,
                    () -> paymentService.processPayment(55.0, amount));
        }
    }

    @Test
    void rejectsAmountDueWithFractionOfACent() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(10.001, 20.0));
    }

    @Test
    void rejectsAmountReceivedWithFractionOfACent() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(10.0, 20.001));
    }
}