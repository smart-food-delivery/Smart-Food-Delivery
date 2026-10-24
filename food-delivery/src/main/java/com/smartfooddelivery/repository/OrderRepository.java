package com.smartfooddelivery.repository;

import com.smartfooddelivery.model.Order;

import java.util.HashMap;
import java.util.Map;

public class OrderRepository {

    private final Map<Integer, Order> orders = new HashMap<>();

    public void save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }

        if (orders.containsKey(order.getId())) {
            throw new IllegalArgumentException(
                    "An order with this ID already exists"
            );
        }

        orders.put(order.getId(), order);
    }

    public Order findById(int id) {
        return orders.get(id);
    }
}