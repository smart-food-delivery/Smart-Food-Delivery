package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.repository.OrderRepository;

import java.util.Objects;

public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public OrderPaymentService(OrderRepository orderRepository,
                               PaymentService paymentService) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.paymentService = Objects.requireNonNull(paymentService);
    }

    public double payOrder(int orderId, double amountReceived) {
        Order order = orderRepository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        order.validatePaymentAllowed();

        double change = paymentService.processPayment(
                order.getTotal(),
                amountReceived
        );

        order.markAsPaid();

        return change;
    }
}