package com.smartfooddelivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CashPaymentService implements PaymentService {

    @Override
    public double processPayment(double amountDue, double amountReceived) {
        BigDecimal due = validateAmount(amountDue, "Amount due");
        BigDecimal received = validateAmount(
                amountReceived, "Amount received"
        );

        if (received.compareTo(due) < 0) {
            throw new IllegalArgumentException(
                    "Received cash is less than the amount due"
            );
        }

        return received.subtract(due).doubleValue();
    }

    private BigDecimal validateAmount(double amount, String fieldName) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be finite and non-negative"
            );
        }

        try {
            return BigDecimal.valueOf(amount)
                    .setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    fieldName + " must have at most two decimal places",
                    exception
            );
        }
    }
}