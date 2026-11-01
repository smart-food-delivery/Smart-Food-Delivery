
package com.smartfooddelivery.model;

public class ElectricVehicleDriver extends Driver {

    private double batteryPercentage;
    private double fullRangeKm;

    public ElectricVehicleDriver(int id, String name,
                                 boolean available,
                                 double distance, double rating) {
        this(id, name, available, distance, rating,
                100.0, 100.0);
    }

    public ElectricVehicleDriver(int id, String name,
                                 boolean available,
                                 double distance, double rating,
                                 double batteryPercentage,
                                 double fullRangeKm) {
        super(id, name, available, distance, rating);

        if (!Double.isFinite(batteryPercentage)
                || batteryPercentage < 0
                || batteryPercentage > 100) {
            throw new IllegalArgumentException(
                    "Invalid battery percentage");
        }

        if (!Double.isFinite(fullRangeKm)
                || fullRangeKm < 0) {
            throw new IllegalArgumentException(
                    "Invalid vehicle range");
        }

        this.batteryPercentage = batteryPercentage;
        this.fullRangeKm = fullRangeKm;
    }

    public double getBatteryPercentage() {
        return batteryPercentage;
    }

    public double getAvailableRangeKm() {
        return fullRangeKm * batteryPercentage / 100.0;
    }

    @Override
    public boolean canDeliver(Order order) {
        if (order == null) {
            return false;
        }

        double requiredDistance =
                getDistance() + order.getDeliveryDistanceKm();

        return Double.isFinite(requiredDistance)
                && requiredDistance >= 0
                && requiredDistance <= getAvailableRangeKm();
    }
}
