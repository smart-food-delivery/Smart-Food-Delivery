
package com.smartfooddelivery.model;

public class BicycleDriver extends Driver {

    private static final double MAX_DELIVERY_DISTANCE_KM = 5.0;

    public BicycleDriver(int id, String name,
                         boolean available,
                         double distance, double rating) {

        super(id, name, available, distance, rating);
    }

    public boolean canDeliver(double deliveryDistanceKm) {

        if (!Double.isFinite(deliveryDistanceKm)
                || deliveryDistanceKm < 0) {
            return false;
        }

        return deliveryDistanceKm <= MAX_DELIVERY_DISTANCE_KM;
    }


    @Override
    public boolean canDeliver(Order order) {
        if (order == null) {
            return false;
        }

        int totalQuantity = order.getItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return canDeliver(order.getDeliveryDistanceKm())
                && totalQuantity <= 3;
    }

}
