package com.smartfooddelivery.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.repository.DriverRepository;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.OrderItem;
import java.util.List;
import com.smartfooddelivery.strategy.NearestDriverStrategy;
import com.smartfooddelivery.strategy.HighestRatedDriverStrategy;
public class DriverServiceTest {

    @Test
    void assignAvailableDriverSuccessfully() {

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        Order order = mock(Order.class);
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(1)).thenReturn(driver);

        driverService.assignDriver(10, 1);

        verify(order).assignDriver(driver);
        assertFalse(driver.isAvailable());
    }

    @Test
    void assignDriverWithInvalidOrderShouldFail() {

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        when(orderRepository.findById(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            driverService.assignDriver(99, 1);
        });
    }

    @Test
    void assignDriverWithInvalidDriverShouldFail() {

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        Order order = mock(Order.class);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            driverService.assignDriver(10, 99);
        });
    }

    @Test
    void assignUnavailableDriverShouldFail() {

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        Order order = mock(Order.class);
        Driver driver = new Driver(1, "Ahmad", false, 2.5, 4.8);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(1)).thenReturn(driver);

        assertThrows(IllegalStateException.class, () -> {
            driverService.assignDriver(10, 1);
        });

        verify(order, never()).assignDriver(any(Driver.class));
    }

    @Test
    void assignDriverToCancelledOrderShouldFail() {

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        Order order = mock(Order.class);
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(1)).thenReturn(driver);
        when(order.getStatus()).thenReturn(OrderStatus.CANCELLED);

        assertThrows(IllegalStateException.class, () -> {
            driverService.assignDriver(10, 1);
        });

        verify(order, never()).assignDriver(any(Driver.class));
        assertTrue(driver.isAvailable());
    }

    @Test
    void assignDriverToOutForDeliveryOrderShouldFail() {

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        Order order = mock(Order.class);
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(1)).thenReturn(driver);
        when(order.getStatus()).thenReturn(OrderStatus.OUT_FOR_DELIVERY);

        assertThrows(IllegalStateException.class, () -> {
            driverService.assignDriver(10, 1);
        });

        verify(order, never()).assignDriver(any(Driver.class));
        assertTrue(driver.isAvailable());
    }

    @Test
    void assignDriverToDeliveredOrderShouldFail() {

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        Order order = mock(Order.class);
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(1)).thenReturn(driver);
        when(order.getStatus()).thenReturn(OrderStatus.DELIVERED);

        assertThrows(IllegalStateException.class, () -> {
            driverService.assignDriver(10, 1);
        });

        verify(order, never()).assignDriver(any(Driver.class));
        assertTrue(driver.isAvailable());
    }

    @Test
    void assignSecondDriverToSameOrderShouldFail() {

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        Order order = mock(Order.class);
        Driver driver = new Driver(2, "Omar", true, 3.0, 4.5);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(2)).thenReturn(driver);

        doThrow(new IllegalStateException("Order already has an assigned driver"))
                .when(order).assignDriver(driver);

        assertThrows(IllegalStateException.class, () -> {
            driverService.assignDriver(10, 2);
        });

        assertTrue(driver.isAvailable());
    }

    @Test
    void assignDriverToRealOrderSuccessfully() {

        MenuItem menuItem = new MenuItem(1, "Pizza", 25.0, 1);
        OrderItem orderItem = new OrderItem(menuItem, 2);

        User customer = mock(User.class);
        Restaurant restaurant = mock(Restaurant.class);
        Order order = new Order(
                10,
                customer,
                restaurant,
                List.of(orderItem),
                "Nablus"
        );
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        when(orderRepository.findById(10)).thenReturn(order);
        when(driverRepository.findById(1)).thenReturn(driver);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        driverService.assignDriver(10, 1);

        assertEquals(driver, order.getAssignedDriver());
        assertFalse(driver.isAvailable());

    }

    @Test
    void assignSecondDriverToRealOrderShouldFail() {

        MenuItem menuItem = new MenuItem(1, "Pizza", 25.0, 1);
        OrderItem orderItem = new OrderItem(menuItem, 2);

        User customer = mock(User.class);
        Restaurant restaurant = mock(Restaurant.class);

        Order order = new Order(
                10,
                customer,
                restaurant,
                List.of(orderItem),
                "Nablus"
        );
        Driver firstDriver = new Driver(1, "Ahmad", true, 2.5, 4.8);
        Driver secondDriver = new Driver(2, "Omar", true, 3.0, 4.5);

        DriverRepository driverRepository = mock(DriverRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);

        when(orderRepository.findById(10)).thenReturn(order);

        when(driverRepository.findById(1)).thenReturn(firstDriver);
        when(driverRepository.findById(2)).thenReturn(secondDriver);

        DriverService driverService =
                new DriverService(driverRepository, orderRepository);

        driverService.assignDriver(10, 1);

        assertThrows(IllegalStateException.class, () -> {
            driverService.assignDriver(10, 2);
        });

        assertEquals(firstDriver, order.getAssignedDriver());
        assertFalse(firstDriver.isAvailable());
        assertTrue(secondDriver.isAvailable());

    }

    @Test
    void automaticallyAssignNearestDriverSuccessfully() {

        Driver nearest = new Driver(1, "Ahmad", true, 1.5, 4.2);
        Driver farther = new Driver(2, "Omar", true, 5.0, 4.9);

        DriverRepository driverRepository = new DriverRepository();
        driverRepository.save(nearest);
        driverRepository.save(farther);

        OrderRepository orderRepository = mock(OrderRepository.class);
        Order order = mock(Order.class);

        when(orderRepository.findById(10)).thenReturn(order);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);

        DriverService service = new DriverService(driverRepository, orderRepository);

        service.assignDriverAutomatically(10, new NearestDriverStrategy());

        verify(order).assignDriver(nearest);
        assertFalse(nearest.isAvailable());
        assertTrue(farther.isAvailable());
    }

    @Test
    void automaticallyAssignHighestRatedDriverSuccessfully() {

        Driver lowerRated = new Driver(1, "Ahmad", true, 1.5, 4.2);
        Driver highestRated = new Driver(2, "Omar", true, 5.0, 4.9);

        DriverRepository driverRepository = new DriverRepository();
        driverRepository.save(lowerRated);
        driverRepository.save(highestRated);

        OrderRepository orderRepository = mock(OrderRepository.class);
        Order order = mock(Order.class);

        when(orderRepository.findById(10)).thenReturn(order);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);

        DriverService service = new DriverService(driverRepository, orderRepository);

        service.assignDriverAutomatically(10, new HighestRatedDriverStrategy());

        verify(order).assignDriver(highestRated);
        assertFalse(highestRated.isAvailable());
        assertTrue(lowerRated.isAvailable());
    }

    @Test
    void automaticallyAssignDriverWithNullStrategyShouldFail() {

        DriverService service = new DriverService(
                new DriverRepository(),
                new OrderRepository()
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.assignDriverAutomatically(10, null));
    }

    @Test
    void automaticallyAssignDriverWhenNoDriversAvailableShouldFail() {

        DriverService service = new DriverService(
                new DriverRepository(),
                new OrderRepository()
        );

        assertThrows(IllegalStateException.class,
                () -> service.assignDriverAutomatically(
                        10, new NearestDriverStrategy()));
    }

}
