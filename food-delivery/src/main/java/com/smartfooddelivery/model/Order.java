package com.smartfooddelivery.model;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.notification.OrderSubject;

import java.util.List;

public class Order extends OrderSubject {

    private final int id;
    private final User customer;
    private final Restaurant restaurant;
    private final List<OrderItem> items;
    private final String deliveryAddress;
    private OrderStatus status;
    private Driver assignedDriver;

    public Order(int id, User customer, Restaurant restaurant,
                 List<OrderItem> items, String deliveryAddress) {

        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }

        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant is required");
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain items");
        }

        if (items.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException("Order items cannot be null");
        }

        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("Delivery address is required");
        }

        this.id = id;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = List.copyOf(items);
        this.deliveryAddress = deliveryAddress.trim();
        this.status = OrderStatus.PENDING;
    }

    public int getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void assignDriver(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver is required");
        }

        if (assignedDriver != null) {
            throw new IllegalStateException(
                    "Order already has an assigned driver"
            );
        }

        if (!driver.isAvailable()) {
            throw new IllegalStateException("Driver is not available");
        }

        this.assignedDriver = driver;
        driver.setAvailable(false);

        notifyDriverAssigned(this, driver);
    }

    public Driver getAssignedDriver() {
        return assignedDriver;
    }

    public double getSubtotal() {
        return items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    public void cancel() {
        if (!status.canBeCancelled()) {
            throw new IllegalStateException(
                    "Order cannot be cancelled in status: " + status
            );
        }

        OrderStatus oldStatus = status;
        status = OrderStatus.CANCELLED;

        notifyStatusChanged(this, oldStatus, status);
    }

    public void changeStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status is required");
        }

        boolean allowed = switch (status) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED;
            case CONFIRMED -> newStatus == OrderStatus.PREPARING;
            case PREPARING -> newStatus == OrderStatus.READY;
            case READY -> newStatus == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Invalid status transition: "
                            + status + " -> " + newStatus
            );
        }

        OrderStatus oldStatus = status;
        status = newStatus;

        notifyStatusChanged(this, oldStatus, newStatus);
    }
}