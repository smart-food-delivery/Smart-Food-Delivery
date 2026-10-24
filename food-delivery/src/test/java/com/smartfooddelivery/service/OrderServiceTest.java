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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    private OrderRepository orderRepository;
    private RestaurantRepository restaurantRepository;
    private OrderService orderService;
    private User customer;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        restaurantRepository = mock(RestaurantRepository.class);

        orderService = new OrderService(
                orderRepository,
                restaurantRepository,
                new DeliveryPolicy(30.0)
        );

        customer = new User(1, "customer1", "hash", "Customer");
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
}