package com.smartfooddelivery.service;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.ExpressOrder;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.ScheduledOrder;
import com.smartfooddelivery.model.StandardOrder;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.notification.OrderObserver;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.repository.RestaurantRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
        this.restaurantRepository =
                Objects.requireNonNull(restaurantRepository);
        this.deliveryPolicy = Objects.requireNonNull(deliveryPolicy);
        this.orderObserver = Objects.requireNonNull(orderObserver);
        this.deliveryCostService = Objects.requireNonNull(deliveryCostService);
    }

    private Order buildValidatedOrder(
            int restaurantId,
            Function<Restaurant, Order> orderFactory) {

        Restaurant restaurant =
                restaurantRepository.findById(restaurantId);

        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant not found");
        }

        if (!restaurant.isAvailable()) {
            throw new IllegalArgumentException("Restaurant is closed");
        }

        Order order = orderFactory.apply(restaurant);

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

    private Order placeWithDelivery(
            int restaurantId,
            double distanceKm,
            Function<Restaurant, Order> orderFactory) {

        if (deliveryCostService == null) {
            throw new IllegalStateException(
                    "Delivery cost service is not configured"
            );
        }

        double fee = deliveryCostService.calculateDeliveryFee(distanceKm);

        Order order = buildValidatedOrder(restaurantId, orderFactory);
        order.setDeliveryDetails(distanceKm, fee);

        // Validate the final total before saving any order type.
        double total = order.getTotal();

        if (!Double.isFinite(total) || total < 0) {
            throw new IllegalArgumentException("Invalid order total");
        }

        orderRepository.save(order);
        return order;
    }

    public Order placeOrder(
            int orderId,
            User customer,
            int restaurantId,
            List<OrderItem> items,
            String deliveryAddress) {

        Order order = buildValidatedOrder(
                restaurantId,
                restaurant -> new StandardOrder(
                        orderId,
                        customer,
                        restaurant,
                        items,
                        deliveryAddress
                )
        );

        orderRepository.save(order);
        return order;
    }

    public Order placeOrderWithDelivery(
            int orderId,
            User customer,
            int restaurantId,
            List<OrderItem> items,
            String deliveryAddress,
            double distanceKm) {

        return placeWithDelivery(
                restaurantId,
                distanceKm,
                restaurant -> new StandardOrder(
                        orderId,
                        customer,
                        restaurant,
                        items,
                        deliveryAddress
                )
        );
    }

    public Order placeExpressOrder(
            int orderId,
            User customer,
            int restaurantId,
            List<OrderItem> items,
            String deliveryAddress,
            double distanceKm,
            double expressFee) {

        return placeWithDelivery(
                restaurantId,
                distanceKm,
                restaurant -> new ExpressOrder(
                        orderId,
                        customer,
                        restaurant,
                        items,
                        deliveryAddress,
                        expressFee
                )
        );
    }

    public Order placeScheduledOrder(
            int orderId,
            User customer,
            int restaurantId,
            List<OrderItem> items,
            String deliveryAddress,
            double distanceKm,
            Instant scheduledDeliveryTime,
            Clock clock) {

        return placeWithDelivery(
                restaurantId,
                distanceKm,
                restaurant -> new ScheduledOrder(
                        orderId,
                        customer,
                        restaurant,
                        items,
                        deliveryAddress,
                        scheduledDeliveryTime,
                        clock
                )
        );
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
}