package com.smartfooddelivery.strategy;

public interface PricingStrategy {

    /**
     * Calculates the final price including delivery.
     *
     * @param subtotal food items subtotal
     * @param deliveryFee delivery charge
     * @return final price after applying the pricing policy
     */
    double calculateTotal(double subtotal, double deliveryFee);
}