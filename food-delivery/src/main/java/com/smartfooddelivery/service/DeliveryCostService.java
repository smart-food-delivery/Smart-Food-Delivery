
package com.smartfooddelivery.service;


public class DeliveryCostService {

    private final double pricePerKm;

    public DeliveryCostService(double pricePerKm) {
        if (!Double.isFinite(pricePerKm) || pricePerKm < 0) {
            throw new IllegalArgumentException(
                    "Price per kilometer must be finite and non-negative"
            );
        }

        this.pricePerKm = pricePerKm;
    }


    public double calculateDeliveryFee(double distanceKm) {
        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException(
                    "Distance must be finite and non-negative"
            );
        }

        return Math.round(distanceKm * pricePerKm * 100.0) / 100.0;
    }
}
