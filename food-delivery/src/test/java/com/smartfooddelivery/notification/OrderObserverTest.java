package com.smartfooddelivery.notification;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderObserverTest {

    private Order order;
    private OrderObserver observer;

    @BeforeEach
    void setUp() {
        User customer = new User(
                1, "customer1", "hash", "Customer"
        );
        Restaurant restaurant = new Restaurant(
                1, "Pizza House", "Nablus", true
        );
        MenuItem item = new MenuItem(1, "Pizza", 40.0, 1);

        order = new Order(
                10,
                customer,
                restaurant,
                List.of(new OrderItem(item, 1)),
                "Nablus, Rafidia, Building 10"
        );

        observer = mock(OrderObserver.class);
        order.addObserver(observer);
    }

    @Test
    void notifiesObserverAfterStatusChanges() {
        order.changeStatus(OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(observer).onStatusChanged(
                order,
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED
        );
        verifyNoMoreInteractions(observer);
    }

    @Test
    void notifiesObserverAfterCancellation() {
        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(observer).onStatusChanged(
                order,
                OrderStatus.PENDING,
                OrderStatus.CANCELLED
        );
        verifyNoMoreInteractions(observer);
    }

    @Test
    void notifiesObserverAfterDriverAssignment() {
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        order.assignDriver(driver);

        assertSame(driver, order.getAssignedDriver());
        verify(observer).onDriverAssigned(order, driver);
        verifyNoMoreInteractions(observer);
    }

    @Test
    void invalidTransitionDoesNotNotifyObserver() {
        assertThrows(
                IllegalStateException.class,
                () -> order.changeStatus(OrderStatus.DELIVERED)
        );

        assertEquals(OrderStatus.PENDING, order.getStatus());
        verifyNoInteractions(observer);
    }

    @Test
    void missingStatusDoesNotNotifyObserver() {
        assertThrows(
                IllegalArgumentException.class,
                () -> order.changeStatus(null)
        );

        assertEquals(OrderStatus.PENDING, order.getStatus());
        verifyNoInteractions(observer);
    }

    @Test
    void rejectedCancellationDoesNotSendAnotherNotification() {
        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.PREPARING);

        clearInvocations(observer);

        assertThrows(
                IllegalStateException.class,
                () -> order.cancel()
        );

        assertEquals(OrderStatus.PREPARING, order.getStatus());
        verifyNoInteractions(observer);
    }

    @Test
    void missingDriverDoesNotNotifyObserver() {
        assertThrows(
                IllegalArgumentException.class,
                () -> order.assignDriver(null)
        );

        assertNull(order.getAssignedDriver());
        verifyNoInteractions(observer);
    }

    @Test
    void secondDriverAssignmentDoesNotSendAnotherNotification() {
        Driver first = new Driver(1, "Ahmad", true, 2.5, 4.8);
        Driver second = new Driver(2, "Omar", true, 3.0, 4.5);

        order.assignDriver(first);
        clearInvocations(observer);

        assertThrows(
                IllegalStateException.class,
                () -> order.assignDriver(second)
        );

        assertSame(first, order.getAssignedDriver());
        verifyNoInteractions(observer);
    }

    @Test
    void duplicateObserverReceivesOnlyOneNotification() {
        order.addObserver(observer);

        order.changeStatus(OrderStatus.CONFIRMED);

        verify(observer, times(1)).onStatusChanged(
                order,
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED
        );
        verifyNoMoreInteractions(observer);
    }

    @Test
    void removedObserverDoesNotReceiveNotifications() {
        order.removeObserver(observer);

        order.changeStatus(OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verifyNoInteractions(observer);
    }

    @Test
    void allRegisteredObserversReceiveNotification() {
        OrderObserver secondObserver = mock(OrderObserver.class);
        order.addObserver(secondObserver);

        order.changeStatus(OrderStatus.CONFIRMED);

        verify(observer).onStatusChanged(
                order,
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED
        );
        verify(secondObserver).onStatusChanged(
                order,
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED
        );
        verifyNoMoreInteractions(observer, secondObserver);
    }

    @Test
    void rejectsMissingObserver() {
        assertThrows(
                IllegalArgumentException.class,
                () -> order.addObserver(null)
        );
    }
}