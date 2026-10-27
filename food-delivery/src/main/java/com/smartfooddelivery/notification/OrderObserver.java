package com.smartfooddelivery.notification;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.Order;

public interface OrderObserver {

    void onStatusChanged(
            Order order,
            OrderStatus oldStatus,
            OrderStatus newStatus
    );

    void onDriverAssigned(Order order, Driver driver);
}