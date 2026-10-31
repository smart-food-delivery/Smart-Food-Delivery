package com.smartfooddelivery.notification;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Base subject in the Observer pattern for order events.
 *
 * <p>Manages observers and notifies them when an order status changes
 * or a driver is assigned. Duplicate observers are not registered.</p>
 *
 * <p>Notifications use a snapshot of the observer list. Changes to
 * subscriptions during a notification affect subsequent events.</p>
 *
 * <p>Observer exceptions propagate to the caller and stop notification
 * of the remaining observers. This class does not undo order changes
 * or retry failed notifications. It is not thread-safe.</p>
 */
public abstract class OrderSubject {

    /**
     * Registered observers, maintained in registration order.
     */
    private final List<OrderObserver> observers = new ArrayList<>();

    /**
     * Registers an observer if it is not already registered.
     *
     * <p>Duplicate detection uses the observer's equality comparison.</p>
     *
     * @param observer the observer to register
     * @throws IllegalArgumentException if the observer is null
     */
    public void addObserver(OrderObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer is required");
        }

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes an observer from future notifications.
     *
     * <p>Passing null or an observer that is not registered has no effect.</p>
     *
     * @param observer the observer to remove
     */
    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies registered observers of a completed order status change.
     *
     * <p>The caller must update the order before invoking this method.
     * Observers are called synchronously in registration order.</p>
     *
     * @param order the order whose status changed
     * @param oldStatus the status before the change
     * @param newStatus the status after the change
     * @throws RuntimeException if an observer fails during notification
     */
    protected void notifyStatusChanged(
            Order order,
            OrderStatus oldStatus,
            OrderStatus newStatus) {

        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.onStatusChanged(order, oldStatus, newStatus);
        }
    }

    /**
     * Notifies registered observers of a completed driver assignment.
     *
     * <p>The caller must record the assignment before invoking this
     * method. Observers are called synchronously in registration order.</p>
     *
     * @param order the order receiving the driver
     * @param driver the assigned driver
     * @throws RuntimeException if an observer fails during notification
     */
    protected void notifyDriverAssigned(Order order, Driver driver) {
        for (OrderObserver observer : new ArrayList<>(observers)) {
            observer.onDriverAssigned(order, driver);
        }
    }
}