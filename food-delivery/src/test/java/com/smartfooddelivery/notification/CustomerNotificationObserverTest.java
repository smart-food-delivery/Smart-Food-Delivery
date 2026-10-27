package com.smartfooddelivery.notification;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomerNotificationObserverTest {

    private NotificationService notificationService;
    private CustomerNotificationObserver observer;
    private Order order;
    private User customer;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        observer = new CustomerNotificationObserver(notificationService);

        customer = new User(1, "customer1", "hash", "Customer");
        order = mock(Order.class);

        // These stubs are used by notification tests only.
        // Plain Mockito mocks allow unused stubs in the constructor test.
        when(order.getId()).thenReturn(10);
        when(order.getCustomer()).thenReturn(customer);
    }

    @Test
    void notifiesCustomerWhenStatusChanges() {
        observer.onStatusChanged(
                order,
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED
        );

        verify(notificationService).sendNotification(
                customer,
                "Order #10 status changed from PENDING to CONFIRMED."
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void notifiesCustomerWhenDriverIsAssigned() {
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        observer.onDriverAssigned(order, driver);

        verify(notificationService).sendNotification(
                customer,
                "Driver Ahmad has been assigned to your order #10."
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void notifiesCustomerWhenOrderIsOutForDelivery() {
        observer.onStatusChanged(
                order,
                OrderStatus.READY,
                OrderStatus.OUT_FOR_DELIVERY
        );

        verify(notificationService).sendNotification(
                customer,
                "Order #10 status changed from READY to OUT_FOR_DELIVERY."
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void notifiesCustomerWhenOrderIsDelivered() {
        observer.onStatusChanged(
                order,
                OrderStatus.OUT_FOR_DELIVERY,
                OrderStatus.DELIVERED
        );

        verify(notificationService).sendNotification(
                customer,
                "Order #10 status changed from OUT_FOR_DELIVERY to DELIVERED."
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void rejectsMissingNotificationService() {
        assertThrows(
                NullPointerException.class,
                () -> new CustomerNotificationObserver(null)
        );
    }
}