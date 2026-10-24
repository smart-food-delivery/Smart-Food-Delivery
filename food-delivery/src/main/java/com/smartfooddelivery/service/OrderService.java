package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.repository.RestaurantRepository;

import java.util.List;
import java.util.Objects;

public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderService(OrderRepository orderRepository,
                        RestaurantRepository restaurantRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.restaurantRepository =
                Objects.requireNonNull(restaurantRepository);
    }

    public Order placeOrder(int orderId, User customer,
                            int restaurantId, List<OrderItem> items,
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
            if (item.getMenuItem().getRestaurantId() != restaurantId) {
                throw new IllegalArgumentException(
                        "All items must belong to the selected restaurant"
                );
            }
        }

        orderRepository.save(order);
        return order;
    }
}