package com.smartfooddelivery.model;

import java.util.List;

public class StandardOrder extends Order {

    public StandardOrder(int id, User customer, Restaurant restaurant,
                         List<OrderItem> items, String deliveryAddress) {

        super(id, customer, restaurant, items, deliveryAddress);
    }
}