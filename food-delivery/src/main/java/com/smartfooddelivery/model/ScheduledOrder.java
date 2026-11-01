package com.smartfooddelivery.model;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class ScheduledOrder extends Order {

    private final Instant scheduledDeliveryTime;

    public ScheduledOrder(
            int id,
            User customer,
            Restaurant restaurant,
            List<OrderItem> items,
            String deliveryAddress,
            Instant scheduledDeliveryTime,
            Clock clock) {

        super(id, customer, restaurant, items, deliveryAddress);

        if (clock == null) {
            throw new IllegalArgumentException("Clock is required");
        }

        if (scheduledDeliveryTime == null) {
            throw new IllegalArgumentException(
                    "Scheduled delivery time is required"
            );
        }

        if (!scheduledDeliveryTime.isAfter(clock.instant())) {
            throw new IllegalArgumentException(
                    "Scheduled delivery time must be in the future"
            );
        }

        this.scheduledDeliveryTime = scheduledDeliveryTime;
    }

    public Instant getScheduledDeliveryTime() {
        return scheduledDeliveryTime;
    }
}