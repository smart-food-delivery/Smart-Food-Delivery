package com.smartfooddelivery.notification;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.Order;

import java.util.ArrayList;
import java.util.List;

public abstract class OrderSubject {

    private final List<OrderObserver> observers = new ArrayList<>();

    public void addObserver(OrderObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer is required");
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    protected void notifyStatusChanged(
            Order order,
            OrderStatus oldStatus,
            OrderStatus newStatus) {

        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.onStatusChanged(order, oldStatus, newStatus);
        }
    }

    protected void notifyDriverAssigned(Order order, Driver driver) {
        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.onDriverAssigned(order, driver);
        }
    }
}