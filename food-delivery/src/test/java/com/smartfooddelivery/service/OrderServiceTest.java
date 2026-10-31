package com.smartfooddelivery.service;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.smartfooddelivery.notification.OrderObserver;
import com.smartfooddelivery.notification.CustomerNotificationObserver;
import com.smartfooddelivery.notification.NotificationService;
import static org.mockito.Mockito.mock;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.repository.DriverRepository;
import com.smartfooddelivery.notification.CustomerNotificationObserver;
import com.smartfooddelivery.notification.NotificationService;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private OrderObserver orderObserver;
    private OrderService orderService;
    private User customer;

    @BeforeEach
    void setUp() {
        this.orderRepository = mock(OrderRepository.class);
        this.restaurantRepository = mock(RestaurantRepository.class);
        this.orderObserver = mock(OrderObserver.class);

        this.orderService = new OrderService(
                this.orderRepository,
                this.restaurantRepository,
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                this.orderObserver
        );

        this.customer = new User(
                1, "customer1", "hash", "Customer"
        );
    }

    private void makeRestaurantAvailable() {
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        when(restaurantRepository.findById(1))
                .thenReturn(restaurant);
    }

    private List<OrderItem> validItems() {
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);
        return List.of(new OrderItem(pizza, 2));
    }

    @Test
    void placesAndSavesValidOrder() {
        makeRestaurantAvailable();

        Order order = orderService.placeOrder(
                10, customer, 1, validItems(),
                "Nablus, Rafidia, Building 10"
        );

        assertEquals(10, order.getId());
        assertSame(customer, order.getCustomer());
        assertEquals(1, order.getRestaurant().getId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(50.0, order.getSubtotal(), 0.001);

        verify(orderRepository).save(order);
    }

    @Test
    void rejectsUnknownRestaurantWithoutSaving() {
        when(restaurantRepository.findById(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 99, validItems(),
                        "Nablus, Rafidia, Building 10"
                ));

        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsClosedRestaurantWithoutSaving() {
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", false);

        when(restaurantRepository.findById(1))
                .thenReturn(restaurant);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 1, validItems(),
                        "Nablus, Rafidia, Building 10"
                ));

        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsItemsFromAnotherRestaurantWithoutSaving() {
        makeRestaurantAvailable();

        MenuItem burger = new MenuItem(2, "Burger", 20.0, 2);
        List<OrderItem> items = List.of(new OrderItem(burger, 1));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 1, items,
                        "Nablus, Rafidia, Building 10"
                ));

        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsEmptyOrderWithoutSaving() {
        makeRestaurantAvailable();

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 1, List.of(),
                        "Nablus, Rafidia, Building 10"
                ));

        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsBlankAddressWithoutSaving() {
        makeRestaurantAvailable();

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 1, validItems(), "   "
                ));

        verifyNoInteractions(orderRepository);
    }
    @Test
    void rejectsUnavailableItemWithoutSaving() {
        makeRestaurantAvailable();

        MenuItem availablePizza =
                new MenuItem(1, "Pizza", 25.0, 1, true);
        MenuItem unavailableBurger =
                new MenuItem(2, "Burger", 20.0, 1, false);

        List<OrderItem> items = List.of(
                new OrderItem(availablePizza, 1),
                new OrderItem(unavailableBurger, 1)
        );

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 1, items,
                        "Nablus, Rafidia, Building 10"
                ));

        verifyNoInteractions(orderRepository);
    }
    @Test
    void rejectsOrderBelowMinimumWithoutSaving() {
        makeRestaurantAvailable();

        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);
        List<OrderItem> items = List.of(new OrderItem(pizza, 1));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 1, items,
                        "Nablus, Rafidia, Building 10"
                ));

        verifyNoInteractions(orderRepository);

    }
    @Test
    void rejectsRestaurantOutsideDeliveryZoneWithoutSaving() {
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Ramallah", true);

        when(restaurantRepository.findById(1))
                .thenReturn(restaurant);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 1, validItems(),
                        "Nablus, Rafidia, Building 10"
                ));

        verifyNoInteractions(orderRepository);
    }
    @Test
    void rejectsIncompleteAddressWithoutSaving() {
        makeRestaurantAvailable();

        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(
                        10, customer, 1, validItems(),
                        "Nablus, Rafidia"
                ));

        verifyNoInteractions(orderRepository);
    }
    private Order createPendingOrder() {
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        return new Order(
                10, customer, restaurant, validItems(),
                "Nablus, Rafidia, Building 10"
        );
    }

    @Test
    void cancelsExistingPendingOrder() {
        Order order = createPendingOrder();

        when(orderRepository.findById(10)).thenReturn(order);

        orderService.cancelOrder(10);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).findById(10);
    }

    @Test
    void rejectsCancellationOfUnknownOrder() {
        when(orderRepository.findById(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.cancelOrder(99));

        verify(orderRepository).findById(99);
    }

    @Test
    void rejectsCancellationOfAlreadyCancelledOrder() {
        Order order = createPendingOrder();
        order.cancel();

        when(orderRepository.findById(10)).thenReturn(order);

        assertThrows(IllegalStateException.class,
                () -> orderService.cancelOrder(10));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }
    @Test
    void automaticallyNotifiesCustomerWhenCreatedOrderStatusChanges() {
        makeRestaurantAvailable();

        NotificationService notificationService =
                mock(NotificationService.class);

        CustomerNotificationObserver customerObserver =
                new CustomerNotificationObserver(notificationService);

        OrderService service = new OrderService(
                orderRepository,
                restaurantRepository,
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                customerObserver
        );

        Order order = service.placeOrder(
                10,
                customer,
                1,
                validItems(),
                "Nablus, Rafidia, Building 10"
        );

        verify(orderRepository).save(order);
        verifyNoInteractions(notificationService);

        order.changeStatus(OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        verify(notificationService).sendNotification(
                customer,
                "Order #10 status changed from PENDING to CONFIRMED."
        );

        verifyNoMoreInteractions(notificationService);
    }
    private Order placeOrderWithNotifications(
            NotificationService notificationService) {

        makeRestaurantAvailable();

        CustomerNotificationObserver customerObserver =
                new CustomerNotificationObserver(notificationService);

        OrderService service = new OrderService(
                orderRepository,
                restaurantRepository,
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                customerObserver
        );

        return service.placeOrder(
                10,
                customer,
                1,
                validItems(),
                "Nablus, Rafidia, Building 10"
        );
    }

    private void moveOrderToReady(Order order) {
        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.PREPARING);
        order.changeStatus(OrderStatus.READY);
    }

    @Test
    void notifiesCustomerWhenDriverIsAssignedThroughService() {
        NotificationService notificationService =
                mock(NotificationService.class);

        Order order = placeOrderWithNotifications(notificationService);

        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);
        DriverRepository driverRepository = mock(DriverRepository.class);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(1)).thenReturn(driver);

        DriverService driverService = new DriverService(
                driverRepository,
                orderRepository
        );

        driverService.assignDriver(10, 1);

        assertSame(driver, order.getAssignedDriver());
        assertFalse(driver.isAvailable());

        verify(notificationService).sendNotification(
                customer,
                "Driver Ahmad has been assigned to your order #10."
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void createdOrderNotifiesCustomerWhenOutForDelivery() {
        NotificationService notificationService =
                mock(NotificationService.class);

        Order order = placeOrderWithNotifications(notificationService);
        moveOrderToReady(order);

        clearInvocations(notificationService);

        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);

        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());

        verify(notificationService).sendNotification(
                customer,
                "Order #10 status changed from READY to OUT_FOR_DELIVERY."
        );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void createdOrderNotifiesCustomerWhenDelivered() {
        NotificationService notificationService =
                mock(NotificationService.class);

        Order order = placeOrderWithNotifications(notificationService);
        moveOrderToReady(order);
        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);

        clearInvocations(notificationService);

        order.changeStatus(OrderStatus.DELIVERED);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());

        verify(notificationService).sendNotification(
                customer,
                "Order #10 status changed from OUT_FOR_DELIVERY to DELIVERED."
        );
        verifyNoMoreInteractions(notificationService);
    }



    @Test
    void updatesOrderStatusSuccessfully() {
        Order order = createPendingOrder();
        when(orderRepository.findById(10)).thenReturn(order);

        orderService.updateOrderStatus(10, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).findById(10);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Order order = createPendingOrder();
        when(orderRepository.findById(10)).thenReturn(order);

        assertThrows(IllegalStateException.class,
                () -> orderService.updateOrderStatus(
                        10, OrderStatus.DELIVERED));

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void rejectsUpdatingUnknownOrder() {
        when(orderRepository.findById(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(
                        99, OrderStatus.CONFIRMED));
    }

    @Test
    void rejectsNullOrderStatus() {
        Order order = createPendingOrder();
        when(orderRepository.findById(10)).thenReturn(order);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(10, null));

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void preventsChangesAfterDelivery() {
        Order order = createPendingOrder();
        when(orderRepository.findById(10)).thenReturn(order);

        order.changeStatus(OrderStatus.CONFIRMED);
        order.changeStatus(OrderStatus.PREPARING);
        order.changeStatus(OrderStatus.READY);
        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.changeStatus(OrderStatus.DELIVERED);

        assertThrows(IllegalStateException.class,
                () -> orderService.updateOrderStatus(
                        10, OrderStatus.CONFIRMED));

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    void doesNotNotifyWhenStatusTransitionIsInvalid() {
        Order order = createPendingOrder();
        order.addObserver(orderObserver);

        when(orderRepository.findById(10)).thenReturn(order);

        assertThrows(IllegalStateException.class,
                () -> orderService.updateOrderStatus(
                        10, OrderStatus.DELIVERED));

        assertEquals(OrderStatus.PENDING, order.getStatus());
        verifyNoInteractions(orderObserver);
    }


    @Test
    void calculatesDeliveryFeeWhenPlacingOrder() {
        makeRestaurantAvailable();

        DeliveryCostService costService = new DeliveryCostService(2.0);

        OrderService service = new OrderService(
                orderRepository,
                restaurantRepository,
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                orderObserver,
                costService
        );

        Order order = service.placeOrderWithDelivery(
                10,
                customer,
                1,
                validItems(),
                "Nablus, Rafidia, Building 10",
                5.0
        );

        assertEquals(5.0, order.getDeliveryDistanceKm(), 0.001);
        assertEquals(10.0, order.getDeliveryFee(), 0.001);
        assertEquals(50.0, order.getSubtotal(), 0.001);
        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void savedOrderIncludesDeliveryFeeInFinalBill() {
        makeRestaurantAvailable();

        DeliveryCostService costService = new DeliveryCostService(2.0);

        OrderService service = new OrderService(
                orderRepository,
                restaurantRepository,
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                orderObserver,
                costService
        );

        Order order = service.placeOrderWithDelivery(
                10,
                customer,
                1,
                validItems(),
                "Nablus, Rafidia, Building 10",
                5.0
        );

        verify(orderRepository).save(order);

        when(orderRepository.findById(10)).thenReturn(order);

        Order savedOrder = orderRepository.findById(10);

        assertEquals(10.0, savedOrder.getDeliveryFee(), 0.001);
        assertEquals(60.0, savedOrder.getTotal(), 0.001);
    }

    @Test
    void rejectsNegativeDeliveryDistanceWithoutSavingOrder() {
        makeRestaurantAvailable();

        DeliveryCostService costService = new DeliveryCostService(2.0);

        OrderService service = new OrderService(
                orderRepository,
                restaurantRepository,
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                orderObserver,
                costService
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.placeOrderWithDelivery(
                        10,
                        customer,
                        1,
                        validItems(),
                        "Nablus, Rafidia, Building 10",
                        -5.0
                ));

        verify(orderRepository, never()).save(any(Order.class));
    }


}
    
