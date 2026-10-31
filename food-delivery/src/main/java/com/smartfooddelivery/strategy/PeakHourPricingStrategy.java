package com.smartfooddelivery.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PeakHourPricingStrategy implements PricingStrategy {

    private final double surchargePercent;

    public PeakHourPricingStrategy(double surchargePercent) {
        if (!Double.isFinite(surchargePercent) || surchargePercent < 0) {
            throw new IllegalArgumentException(
                    "Surcharge percentage must be finite and non-negative"
            );
        }

        this.surchargePercent = surchargePercent;
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

        BigDecimal multiplier = BigDecimal.ONE.add(
                BigDecimal.valueOf(surchargePercent).movePointLeft(2)
        );

        BigDecimal adjustedDeliveryFee = BigDecimal.valueOf(deliveryFee)
                .multiply(multiplier);

        double total = BigDecimal.valueOf(subtotal)
                .add(adjustedDeliveryFee)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException("Total is too large");
        }

        return total;
    }
}