package com.smartfooddelivery.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NormalPricingStrategy implements PricingStrategy {

    @Override
    public double calculateTotal(double subtotal, double deliveryFee) {
        if (!Double.isFinite(subtotal) || subtotal < 0) {
            throw new IllegalArgumentException(
                    "Subtotal must be finite and non-negative"
            );
        }

        if (!Double.isFinite(deliveryFee) || deliveryFee < 0) {
            throw new IllegalArgumentException(
                    "Delivery fee must be finite and non-negative"
            );
        }

        double total = BigDecimal.valueOf(subtotal)
                .add(BigDecimal.valueOf(deliveryFee))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException("Total is too large");
        }

        return total;
    }
}