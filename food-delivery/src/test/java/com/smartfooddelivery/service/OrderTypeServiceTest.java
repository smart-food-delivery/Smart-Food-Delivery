package com.smartfooddelivery.service;

import com.smartfooddelivery.model.*;
import com.smartfooddelivery.notification.OrderObserver;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderTypeServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-11-01T10:00:00Z");

    private static final String ADDRESS =
            "Nablus, Rafidia, Building 10";

    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private OrderService orderService;
    private User customer;
    private Clock clock;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        restaurantRepository = mock(RestaurantRepository.class);
        OrderObserver observer = mock(OrderObserver.class);

        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(NOW);

        customer = new User(
                1, "customer", "hash", "Customer"
        );

        when(restaurantRepository.findById(1))
                .thenReturn(new Restaurant(
                        1, "Pizza House", "Nablus", true
                ));

        orderService = new OrderService(
                orderRepository,
                restaurantRepository,
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                observer,
                new DeliveryCostService(2.0)
        );
    }

    private List<OrderItem> validItems() {
        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1
        );

        return List.of(new OrderItem(pizza, 2));
    }

    private Order placeByType(
            String type,
            List<OrderItem> items,
            String address) {

        return switch (type) {
            case "standard" -> orderService.placeOrderWithDelivery(
                    10, customer, 1, items, address, 5.0
            );

            case "express" -> orderService.placeExpressOrder(
                    10, customer, 1, items, address, 5.0, 5.0
            );

            case "scheduled" -> orderService.placeScheduledOrder(
                    10, customer, 1, items, address, 5.0,
                    NOW.plusSeconds(3600), clock
            );

            default -> throw new IllegalArgumentException(
                    "Unknown test order type: " + type
            );
        };
    }

    private void assertRejectedForAllTypes(
            List<OrderItem> items,
            String address) {

        for (String type : List.of(
                "standard", "express", "scheduled")) {

            assertThrows(
                    IllegalArgumentException.class,
                    () -> placeByType(type, items, address),
                    "Expected rejection for: " + type
            );
        }

        verifyNoInteractions(orderRepository);
    }

    @Test
    void placesStandardOrderWithoutDeliveryCalculation() {
        Order order = orderService.placeOrder(
                10, customer, 1, validItems(), ADDRESS
        );

        assertTrue(order instanceof StandardOrder);
        assertEquals(50.0, order.getTotal(), 0.001);

        verify(orderRepository).save(order);
    }

    @Test
    void placesStandardOrderWithDeliveryFee() {
        Order order = placeByType(
                "standard", validItems(), ADDRESS
        );

        assertTrue(order instanceof StandardOrder);
        assertEquals(5.0, order.getDeliveryDistanceKm(), 0.001);
        assertEquals(10.0, order.getDeliveryFee(), 0.001);
        assertEquals(60.0, order.getTotal(), 0.001);

        verify(orderRepository).save(order);
    }

    @Test
    void placesExpressOrderWithAdditionalFee() {
        Order order = placeByType(
                "express", validItems(), ADDRESS
        );

        assertTrue(order instanceof ExpressOrder);
        assertEquals(
                5.0,
                ((ExpressOrder) order).getExpressFee(),
                0.001
        );
        assertEquals(10.0, order.getDeliveryFee(), 0.001);
        assertEquals(65.0, order.getTotal(), 0.001);

        verify(orderRepository).save(order);
    }

    @Test
    void placesScheduledOrderWithFutureTime() {
        Order order = placeByType(
                "scheduled", validItems(), ADDRESS
        );

        assertTrue(order instanceof ScheduledOrder);
        assertEquals(
                NOW.plusSeconds(3600),
                ((ScheduledOrder) order).getScheduledDeliveryTime()
        );
        assertEquals(60.0, order.getTotal(), 0.001);

        verify(clock).instant();
        verify(orderRepository).save(order);
    }

    @Test
    void rejectsPastScheduledTimeWithoutSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.placeScheduledOrder(
                        10, customer, 1, validItems(), ADDRESS,
                        5.0, NOW.minusSeconds(1), clock
                )
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsInvalidExpressFeeWithoutSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.placeExpressOrder(
                        10, customer, 1, validItems(), ADDRESS,
                        5.0, -1.0
                )
        );

        verifyNoInteractions(orderRepository);
    }

    @Test
    void rejectsBelowMinimumSubtotalForEveryType() {
        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1
        );

        // Delivery and express fees must not satisfy
        // the minimum food subtotal of 30.
        assertRejectedForAllTypes(
                List.of(new OrderItem(pizza, 1)),
                ADDRESS
        );
    }

    @Test
    void rejectsRestaurantOutsideZoneForEveryType() {
        when(restaurantRepository.findById(1))
                .thenReturn(new Restaurant(
                        1, "Pizza House", "Ramallah", true
                ));

        assertRejectedForAllTypes(validItems(), ADDRESS);
    }

    @Test
    void rejectsIncompleteAddressForEveryType() {
        assertRejectedForAllTypes(
                validItems(), "Nablus, Rafidia"
        );
    }

    @Test
    void rejectsUnavailableItemForEveryType() {
        MenuItem unavailablePizza = new MenuItem(
                1, "Pizza", 25.0, 1, false
        );

        assertRejectedForAllTypes(
                List.of(new OrderItem(unavailablePizza, 2)),
                ADDRESS
        );
    }

    @Test
    void rejectsItemsFromAnotherRestaurantForEveryType() {
        MenuItem otherRestaurantPizza = new MenuItem(
                1, "Pizza", 25.0, 2
        );

        assertRejectedForAllTypes(
                List.of(new OrderItem(otherRestaurantPizza, 2)),
                ADDRESS
        );
    }

    @Test
    void rejectsClosedRestaurantForEveryType() {
        when(restaurantRepository.findById(1))
                .thenReturn(new Restaurant(
                        1, "Pizza House", "Nablus", false
                ));

        assertRejectedForAllTypes(validItems(), ADDRESS);
    }
    @Test
    void rejectsDeliveryOrderWhenDeliveryCostServiceIsMissing() {
        OrderService serviceWithoutDeliveryCost = new OrderService(
                orderRepository,
                restaurantRepository,
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                mock(OrderObserver.class)
        );

        assertThrows(
                IllegalStateException.class,
                () -> serviceWithoutDeliveryCost.placeOrderWithDelivery(
                        10,
                        customer,
                        1,
                        validItems(),
                        ADDRESS,
                        5.0
                )
        );

        verifyNoInteractions(orderRepository);
    }
}