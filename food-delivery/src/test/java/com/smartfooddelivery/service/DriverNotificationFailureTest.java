package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.notification.CustomerNotificationObserver;
import com.smartfooddelivery.notification.NotificationService;
import com.smartfooddelivery.repository.DriverRepository;
import com.smartfooddelivery.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DriverNotificationFailureTest {

    private Order createOrder(int id, User customer) {
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        return new Order(
                id,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );
    }

    @Test
    void failedEmailKeepsDriverBusyAndPreventsAnotherAssignment() {
        User customer = new User(
                1, "customer", "hash",
                "Customer", "customer@example.com"
        );

        Order firstOrder = createOrder(10, customer);
        Order secondOrder = createOrder(11, customer);

        Driver driver = new Driver(
                1, "Ahmad", true, 2.0, 4.8
        );

        NotificationService notifications =
                mock(NotificationService.class);

        IllegalStateException emailFailure =
                new IllegalStateException("Simulated email failure");

        doThrow(emailFailure)
                .when(notifications)
                .sendNotification(eq(customer), anyString());

        firstOrder.addObserver(
                new CustomerNotificationObserver(notifications)
        );

        OrderRepository orders = new OrderRepository();
        orders.save(firstOrder);
        orders.save(secondOrder);

        DriverRepository drivers = new DriverRepository();
        drivers.save(driver);

        DriverService service = new DriverService(drivers, orders);

        IllegalStateException actualFailure = assertThrows(
                IllegalStateException.class,
                () -> service.assignDriver(10, 1)
        );

        assertSame(emailFailure, actualFailure);
        assertSame(driver, firstOrder.getAssignedDriver());
        assertFalse(driver.isAvailable());
        assertTrue(drivers.findAvailableDrivers().isEmpty());

        assertThrows(
                IllegalStateException.class,
                () -> service.assignDriver(11, 1)
        );

        assertNull(secondOrder.getAssignedDriver());
        assertSame(driver, firstOrder.getAssignedDriver());

        verify(notifications).sendNotification(
                eq(customer), anyString()
        );
        verifyNoMoreInteractions(notifications);
    }

    @Test
    void directAssignmentRejectsBusyDriverWithoutNotification() {
        User customer = new User(
                1, "customer", "hash",
                "Customer", "customer@example.com"
        );

        Order order = createOrder(10, customer);
        Driver busyDriver = new Driver(
                1, "Ahmad", false, 2.0, 4.8
        );

        NotificationService notifications =
                mock(NotificationService.class);

        order.addObserver(
                new CustomerNotificationObserver(notifications)
        );

        assertThrows(
                IllegalStateException.class,
                () -> order.assignDriver(busyDriver)
        );

        assertNull(order.getAssignedDriver());
        assertFalse(busyDriver.isAvailable());
        verifyNoInteractions(notifications);
    }
}