package com.smartfooddelivery.service;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.domain.PaymentStatus;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.strategy.PromotionalPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderPaymentServiceTest {

    private OrderRepository orderRepository;
    private PaymentService paymentService;
    private OrderPaymentService orderPaymentService;

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepository();
        paymentService = mock(PaymentService.class);

        orderPaymentService = new OrderPaymentService(
                orderRepository, paymentService
        );
    }

    private Order createOrder() {
        User customer = new User(1, "customer", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        Order order = new Order(
                10,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        order.setDeliveryDetails(5.0, 10.0);
        orderRepository.save(order);

        return order;
    }

    private void moveToOutForDelivery(Order order) {
        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.PREPARING);
        order.changeStatus(OrderStatus.READY);
        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);
    }

    @Test
    void newOrderStartsUnpaid() {
        Order order = createOrder();

        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
    }

    @Test
    void successfulPaymentRecordsPaidStatusAndReturnsChange() {
        Order order = createOrder();
        moveToOutForDelivery(order);

        when(paymentService.processPayment(60.0, 70.0))
                .thenReturn(10.0);

        double change = orderPaymentService.payOrder(10, 70.0);

        assertEquals(10.0, change, 0.001);
        assertEquals(PaymentStatus.PAID,
                orderRepository.findById(10).getPaymentStatus());
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());

        verify(paymentService).processPayment(60.0, 70.0);
    }

    @Test
    void chargesTotalAfterDiscountIncludingDelivery() {
        Order order = createOrder();
        order.setPricingStrategy(new PromotionalPricingStrategy(10.0));
        moveToOutForDelivery(order);

        when(paymentService.processPayment(55.0, 60.0))
                .thenReturn(5.0);

        double change = orderPaymentService.payOrder(10, 60.0);

        assertEquals(5.0, change, 0.001);
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        verify(paymentService).processPayment(55.0, 60.0);
    }

    @Test
    void failedPaymentLeavesOrderUnpaid() {
        Order order = createOrder();
        moveToOutForDelivery(order);

        IllegalArgumentException failure =
                new IllegalArgumentException("Insufficient cash");

        when(paymentService.processPayment(60.0, 50.0))
                .thenThrow(failure);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> orderPaymentService.payOrder(10, 50.0)
        );

        assertSame(failure, thrown);
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());
    }

    @Test
    void rejectsUnknownOrderWithoutProcessingPayment() {
        assertThrows(IllegalArgumentException.class,
                () -> orderPaymentService.payOrder(99, 60.0));

        verifyNoInteractions(paymentService);
    }

    @Test
    void rejectsPaymentBeforeOrderIsOutForDelivery() {
        Order order = createOrder();

        assertThrows(IllegalStateException.class,
                () -> orderPaymentService.payOrder(10, 60.0));

        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        verifyNoInteractions(paymentService);
    }

    @Test
    void rejectsPaymentForCancelledOrder() {
        Order order = createOrder();
        order.cancel();

        assertThrows(IllegalStateException.class,
                () -> orderPaymentService.payOrder(10, 60.0));

        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        verifyNoInteractions(paymentService);
    }

    @Test
    void rejectsPaymentForDeliveredOrder() {
        Order order = createOrder();
        moveToOutForDelivery(order);
        order.changeStatus(OrderStatus.DELIVERED);

        assertThrows(IllegalStateException.class,
                () -> orderPaymentService.payOrder(10, 60.0));

        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        verifyNoInteractions(paymentService);
    }

    @Test
    void rejectsDuplicatePaymentWithoutProcessingAgain() {
        Order order = createOrder();
        moveToOutForDelivery(order);

        when(paymentService.processPayment(60.0, 60.0))
                .thenReturn(0.0);

        orderPaymentService.payOrder(10, 60.0);

        assertThrows(IllegalStateException.class,
                () -> orderPaymentService.payOrder(10, 60.0));

        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        verify(paymentService, times(1)).processPayment(60.0, 60.0);
    }

    @Test
    void worksWithRealCashPaymentService() {
        Order order = createOrder();
        moveToOutForDelivery(order);

        OrderPaymentService realService = new OrderPaymentService(
                orderRepository, new CashPaymentService()
        );

        assertThrows(IllegalArgumentException.class,
                () -> realService.payOrder(10, 59.99));

        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());

        double change = realService.payOrder(10, 65.0);

        assertEquals(5.0, change, 0.001);
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
    }
}