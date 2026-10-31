package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.notification.OrderObserver;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.repository.RestaurantRepository;

import java.util.List;
import java.util.Objects;

public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPolicy deliveryPolicy;
    private final OrderObserver orderObserver;

    public OrderService(
            OrderRepository orderRepository,
            RestaurantRepository restaurantRepository,
            DeliveryPolicy deliveryPolicy,
            OrderObserver orderObserver) {

        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.restaurantRepository =
                Objects.requireNonNull(restaurantRepository);
        this.deliveryPolicy = Objects.requireNonNull(deliveryPolicy);
        this.orderObserver = Objects.requireNonNull(orderObserver);
    }

    public Order placeOrder(
            int orderId,
            User customer,
            int restaurantId,
            List<OrderItem> items,
            String deliveryAddress) {

        Restaurant restaurant =
                restaurantRepository.findById(restaurantId);

        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant not found");
        }

        if (!restaurant.isAvailable()) {
            throw new IllegalArgumentException("Restaurant is closed");
        }

        Order order = new Order(
                orderId,
                customer,
                restaurant,
                items,
                deliveryAddress
        );

        for (OrderItem item : order.getItems()) {
            if (!item.getMenuItem().isAvailable()) {
                throw new IllegalArgumentException(
                        "Order contains an unavailable item"
                );
            }

            if (item.getMenuItem().getRestaurantId() != restaurantId) {
                throw new IllegalArgumentException(
                        "All items must belong to the selected restaurant"
                );
            }
        }

        deliveryPolicy.validate(order);
        order.addObserver(orderObserver);
        orderRepository.save(order);

        return order;
    }

    public void cancelOrder(int orderId) {
        Order order = orderRepository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        order.cancel();
    }
}