package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Order;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DeliveryPolicy {

    private final double minimumOrderValue;
    private final Set<String> allowedCities;

    public DeliveryPolicy(double minimumOrderValue,
                          Set<String> allowedCities) {

        if (!Double.isFinite(minimumOrderValue)
                || minimumOrderValue < 0) {
            throw new IllegalArgumentException(
                    "Minimum order value must be finite and non-negative"
            );
        }

        if (allowedCities == null || allowedCities.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one delivery city is required"
            );
        }

        Set<String> normalizedCities = new HashSet<>();

        for (String city : allowedCities) {
            if (city == null || city.isBlank()) {
                throw new IllegalArgumentException(
                        "Delivery city cannot be blank"
                );
            }

            normalizedCities.add(normalize(city));
        }

        this.minimumOrderValue = minimumOrderValue;
        this.allowedCities = Set.copyOf(normalizedCities);
    }

    public void validate(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }

        String restaurantCity = order.getRestaurant().getAddress();

        if (restaurantCity == null
                || !allowedCities.contains(normalize(restaurantCity))) {
            throw new IllegalArgumentException(
                    "Restaurant is outside the delivery zone"
            );
        }

        double subtotal = order.getSubtotal();

        if (!Double.isFinite(subtotal)
                || subtotal < minimumOrderValue) {
            throw new IllegalArgumentException(
                    "Order does not meet the minimum order value"
            );
        }
    }

    private String normalize(String city) {
        return city.trim().toLowerCase(Locale.ROOT);
    }
}