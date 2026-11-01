
package com.smartfooddelivery.model;

public class CarDriver extends Driver {

    public CarDriver(int id, String name,
                     boolean available,
                     double distance, double rating) {
        super(id, name, available, distance, rating);
    }

    @Override
    public boolean canDeliver(Order order) {
        if (order == null) {
            return false;
        }

        return Double.isFinite(order.getDeliveryDistanceKm())
                && order.getDeliveryDistanceKm() >= 0;
    }
}
