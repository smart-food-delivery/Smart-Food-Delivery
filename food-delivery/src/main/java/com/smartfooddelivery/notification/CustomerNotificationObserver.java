package com.smartfooddelivery.notification;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.Order;

import java.util.Objects;

public class CustomerNotificationObserver implements OrderObserver {

    private final NotificationService notificationService;

    public static CustomerNotificationObserver fromEnvironment() {
        return new CustomerNotificationObserver(EmailNotificationService.fromEnvironment());
    }

    public CustomerNotificationObserver(
            NotificationService notificationService) {

        this.notificationService = Objects.requireNonNull(
                notificationService,
                "Notification service is required"
        );
    }

    @Override
    public void onStatusChanged(
            Order order,
            OrderStatus oldStatus,
            OrderStatus newStatus) {

        String message = "Order #" + order.getId()
                + " status changed from " + oldStatus
                + " to " + newStatus + ".";

        notificationService.sendNotification(
                order.getCustomer(),
                message
        );
    }

    @Override
    public void onDriverAssigned(Order order, Driver driver) {

        String message = "Driver " + driver.getName()
                + " has been assigned to your order #"
                + order.getId() + ".";

        notificationService.sendNotification(
                order.getCustomer(),
                message
        );
    }
}
