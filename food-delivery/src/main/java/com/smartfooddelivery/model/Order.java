package com.smartfooddelivery.model;

import com.smartfooddelivery.domain.OrderStatus;

import java.util.List;

public class Order {

    private final int id;
    private final User customer;
    private final Restaurant restaurant;
    private final List<OrderItem> items;
    private final String deliveryAddress;
    private OrderStatus status;

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

    public double getSubtotal() {
        return items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }
}