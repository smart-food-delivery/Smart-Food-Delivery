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
import com.smartfooddelivery.domain.OrderStatus;

public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPolicy deliveryPolicy;
    private final OrderObserver orderObserver;
    private final DeliveryCostService deliveryCostService;

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
        this.deliveryCostService = null;
    }

    public OrderService(
            OrderRepository orderRepository,
            RestaurantRepository restaurantRepository,
            DeliveryPolicy deliveryPolicy,
            OrderObserver orderObserver,
            DeliveryCostService deliveryCostService) {

        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.restaurantRepository = Objects.requireNonNull(restaurantRepository);
        this.deliveryPolicy = Objects.requireNonNull(deliveryPolicy);
        this.orderObserver = Objects.requireNonNull(orderObserver);
        this.deliveryCostService = Objects.requireNonNull(deliveryCostService);
    }


    private Order buildValidatedOrder(
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


        return order;
    }


    public Order placeOrder(
            int orderId,
            User customer,
            int restaurantId,
            List<OrderItem> items,
            String deliveryAddress) {

        Order order = buildValidatedOrder(
                orderId,
                customer,
                restaurantId,
                items,
                deliveryAddress
        );

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


    public void updateOrderStatus(int orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        order.changeStatus(newStatus);
    }


    /**
     * Places an order with a delivery fee calculated
     * from the delivery distance.
     *
     * @param orderId order identifier
     * @param customer customer placing the order
     * @param restaurantId selected restaurant
     * @param items ordered items
     * @param deliveryAddress delivery destination
     * @param distanceKm delivery distance in kilometers
     * @return created order including delivery fee
     */
    public Order placeOrderWithDelivery(
            int orderId,
            User customer,
            int restaurantId,
            List<OrderItem> items,
            String deliveryAddress,
            double distanceKm) {

        if (deliveryCostService == null) {
            throw new IllegalStateException(
                    "Delivery cost service is not configured"
            );
        }

        double fee = deliveryCostService.calculateDeliveryFee(distanceKm);


        Order order = buildValidatedOrder(
                orderId,
                customer,
                restaurantId,
                items,
                deliveryAddress
        );

        order.setDeliveryDetails(distanceKm, fee);

        orderRepository.save(order);

        return order;

    }


}