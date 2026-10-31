package com.smartfooddelivery.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PromotionalPricingStrategy implements PricingStrategy {

    private final double discountPercent;

    public PromotionalPricingStrategy(double discountPercent) {
        if (!Double.isFinite(discountPercent)
                || discountPercent < 0
                || discountPercent > 100) {
            throw new IllegalArgumentException(
                    "Discount percentage must be between 0 and 100"
            );
        }

        this.discountPercent = discountPercent;
    }

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

        BigDecimal discountRate = BigDecimal.valueOf(discountPercent)
                .movePointLeft(2);

        BigDecimal discountedSubtotal = BigDecimal.valueOf(subtotal)
                .multiply(BigDecimal.ONE.subtract(discountRate));

        double total = discountedSubtotal
                .add(BigDecimal.valueOf(deliveryFee))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException("Total is too large");
        }

        return total;
    }
}