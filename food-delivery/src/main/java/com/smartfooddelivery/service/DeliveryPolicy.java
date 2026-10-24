package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Order;

public class DeliveryPolicy {

    private final double minimumOrderValue;

    public DeliveryPolicy(double minimumOrderValue) {
        if (!Double.isFinite(minimumOrderValue)
                || minimumOrderValue < 0) {
            throw new IllegalArgumentException(
                    "Minimum order value must be finite and non-negative"
            );
        }

        this.minimumOrderValue = minimumOrderValue;
    }

    public void validate(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }

        double subtotal = order.getSubtotal();

        if (!Double.isFinite(subtotal)
                || subtotal < minimumOrderValue) {
            throw new IllegalArgumentException(
                    "Order does not meet the minimum order value"
            );
        }
    }
}