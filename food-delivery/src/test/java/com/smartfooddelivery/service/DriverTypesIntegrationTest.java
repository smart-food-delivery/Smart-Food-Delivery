
package com.smartfooddelivery.service;

import com.smartfooddelivery.model.*;
import com.smartfooddelivery.repository.DriverRepository;
import com.smartfooddelivery.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import com.smartfooddelivery.strategy.NearestDriverStrategy;
import com.smartfooddelivery.strategy.HighestRatedDriverStrategy;
class DriverTypesIntegrationTest {

    @Test
    void repositorySupportsAllDriverTypes() {
        DriverRepository repository = new DriverRepository();

        repository.save(new BicycleDriver(
                1, "Ahmad", true, 2.0, 4.5));
        repository.save(new MotorcycleDriver(
                2, "Omar", true, 3.0, 4.7));
        repository.save(new CarDriver(
                3, "Ali", true, 4.0, 4.9));
        repository.save(new ElectricVehicleDriver(
                4, "Sara", true, 5.0, 4.8));

        List<Driver> available = repository.findAvailableDrivers();

        assertEquals(4, available.size());
        assertInstanceOf(BicycleDriver.class, repository.findById(1));
        assertInstanceOf(MotorcycleDriver.class, repository.findById(2));
        assertInstanceOf(CarDriver.class, repository.findById(3));
        assertInstanceOf(ElectricVehicleDriver.class, repository.findById(4));
    }

    @Test
    void assignedDriverKeepsItsSpecificType() {
        DriverRepository driverRepository = new DriverRepository();
        OrderRepository orderRepository = new OrderRepository();

        Driver driver = new CarDriver(
                3, "Ali", true, 4.0, 4.9);
        driverRepository.save(driver);

        User customer = new User(
                1, "customer1", "hash", "Customer");
        Restaurant restaurant = new Restaurant(
                1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1);

        Order order = new Order(
                10,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        orderRepository.save(order);

        DriverService service = new DriverService(
                driverRepository, orderRepository);

        service.assignDriver(10, 3);

        assertSame(driver, order.getAssignedDriver());
        assertInstanceOf(CarDriver.class, order.getAssignedDriver());
        assertFalse(driver.isAvailable());
    }


    @Test
    void nearestStrategySupportsDifferentDriverTypes() {

        Driver bicycle = new BicycleDriver(
                1, "Ahmad", true, 5.0, 4.5);

        Driver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 2.0, 4.7);

        Driver car = new CarDriver(
                3, "Ali", true, 8.0, 4.9);

        Driver electric = new ElectricVehicleDriver(
                4, "Sara", true, 3.0, 4.8);

        NearestDriverStrategy strategy =
                new NearestDriverStrategy();

        Driver selected = strategy.selectDriver(
                List.of(bicycle, motorcycle, car, electric)
        );

        assertSame(motorcycle, selected);
        assertInstanceOf(MotorcycleDriver.class, selected);
        assertEquals(2, selected.getId());
    }


    @Test
    void highestRatedStrategySupportsDifferentDriverTypes() {

        Driver bicycle = new BicycleDriver(
                1, "Ahmad", true, 5.0, 4.2);

        Driver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 2.0, 4.6);

        Driver car = new CarDriver(
                3, "Ali", true, 8.0, 4.9);

        Driver electric = new ElectricVehicleDriver(
                4, "Sara", true, 3.0, 4.7);

        HighestRatedDriverStrategy strategy =
                new HighestRatedDriverStrategy();

        Driver selected = strategy.selectDriver(
                List.of(bicycle, motorcycle, car, electric)
        );

        assertSame(car, selected);
        assertInstanceOf(CarDriver.class, selected);
        assertEquals(4.9, selected.getRating(), 0.001);
    }


    @Test
    void automaticallyAssignsNearestDriverOfDifferentTypes() {

        DriverRepository driverRepository = new DriverRepository();
        OrderRepository orderRepository = new OrderRepository();

        Driver bicycle = new BicycleDriver(
                1, "Ahmad", true, 5.0, 4.5);

        Driver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 2.0, 4.7);

        Driver car = new CarDriver(
                3, "Ali", true, 8.0, 4.9);

        driverRepository.save(bicycle);
        driverRepository.save(motorcycle);
        driverRepository.save(car);

        User customer = new User(
                1, "customer1", "hash", "Customer");

        Restaurant restaurant = new Restaurant(
                1, "Pizza House", "Nablus", true);

        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1);

        Order order = new Order(
                20,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        orderRepository.save(order);

        DriverService service = new DriverService(
                driverRepository, orderRepository);

        service.assignDriverAutomatically(
                20, new NearestDriverStrategy()
        );

        assertSame(motorcycle, order.getAssignedDriver());
        assertInstanceOf(
                MotorcycleDriver.class,
                order.getAssignedDriver()
        );
        assertFalse(motorcycle.isAvailable());
        assertTrue(bicycle.isAvailable());
        assertTrue(car.isAvailable());
    }


    @Test
    void rejectsBicycleForLongDeliveryDistance() {

        DriverRepository driverRepository = new DriverRepository();
        OrderRepository orderRepository = new OrderRepository();

        BicycleDriver bicycle = new BicycleDriver(
                1, "Ahmad", true, 2.0, 4.5
        );

        driverRepository.save(bicycle);

        User customer = new User(
                1, "customer1", "hash", "Customer");

        Restaurant restaurant = new Restaurant(
                1, "Pizza House", "Nablus", true);

        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1);

        Order order = new Order(
                30,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        order.setDeliveryDetails(8.0, 10.0);
        orderRepository.save(order);

        DriverService service = new DriverService(
                driverRepository, orderRepository
        );

        assertThrows(IllegalStateException.class,
                () -> service.assignDriver(30, 1));

        assertNull(order.getAssignedDriver());
        assertTrue(bicycle.isAvailable());
    }

    @Test
    void allowsBicycleForShortDeliveryDistance() {

        DriverRepository driverRepository = new DriverRepository();
        OrderRepository orderRepository = new OrderRepository();

        BicycleDriver bicycle = new BicycleDriver(
                1, "Ahmad", true, 2.0, 4.5
        );

        driverRepository.save(bicycle);

        User customer = new User(
                1, "customer1", "hash", "Customer");

        Restaurant restaurant = new Restaurant(
                1, "Pizza House", "Nablus", true);

        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1);

        Order order = new Order(
                31,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        order.setDeliveryDetails(3.0, 5.0);
        orderRepository.save(order);

        DriverService service = new DriverService(
                driverRepository, orderRepository
        );

        service.assignDriver(31, 1);

        assertSame(bicycle, order.getAssignedDriver());
        assertFalse(bicycle.isAvailable());
    }


    @Test
    void rejectsMotorcycleForLongDeliveryDistance() {

        DriverRepository driverRepository = new DriverRepository();
        OrderRepository orderRepository = new OrderRepository();

        MotorcycleDriver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 3.0, 4.7
        );

        driverRepository.save(motorcycle);

        User customer = new User(
                1, "customer1", "hash", "Customer");

        Restaurant restaurant = new Restaurant(
                1, "Pizza House", "Nablus", true);

        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1);

        Order order = new Order(
                40,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        order.setDeliveryDetails(18.0, 15.0);
        orderRepository.save(order);

        DriverService service = new DriverService(
                driverRepository, orderRepository
        );

        assertThrows(IllegalStateException.class,
                () -> service.assignDriver(40, 2));

        assertNull(order.getAssignedDriver());
        assertTrue(motorcycle.isAvailable());
    }

    @Test
    void allowsMotorcycleForMediumDeliveryDistance() {

        DriverRepository driverRepository = new DriverRepository();
        OrderRepository orderRepository = new OrderRepository();

        MotorcycleDriver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 3.0, 4.7
        );

        driverRepository.save(motorcycle);

        User customer = new User(
                1, "customer1", "hash", "Customer");

        Restaurant restaurant = new Restaurant(
                1, "Pizza House", "Nablus", true);

        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1);

        Order order = new Order(
                41,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        order.setDeliveryDetails(10.0, 8.0);
        orderRepository.save(order);

        DriverService service = new DriverService(
                driverRepository, orderRepository
        );

        service.assignDriver(41, 2);

        assertSame(motorcycle, order.getAssignedDriver());
        assertFalse(motorcycle.isAvailable());
    }


    @Test
    void bicycleRejectsLargeOrders() {
        Order order = createOrder(50, 4, 3.0);

        BicycleDriver bicycle = new BicycleDriver(
                1, "Ahmad", true, 2.0, 4.5);

        assertFalse(bicycle.canDeliver(order));
    }

    @Test
    void motorcycleRejectsLargeOrders() {
        Order order = createOrder(51, 9, 10.0);

        MotorcycleDriver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 3.0, 4.7);

        assertFalse(motorcycle.canDeliver(order));
    }

    @Test
    void carAcceptsLargeOrders() {
        Order order = createOrder(52, 20, 25.0);

        CarDriver car = new CarDriver(
                3, "Ali", true, 4.0, 4.9);

        assertTrue(car.canDeliver(order));
    }

    @Test
    void electricVehicleAcceptsSufficientBattery() {
        Order order = createOrder(53, 2, 20.0);

        ElectricVehicleDriver electric =
                new ElectricVehicleDriver(
                        4, "Sara", true, 5.0, 4.8,
                        80.0, 100.0);

        assertTrue(electric.canDeliver(order));
    }

    @Test
    void electricVehicleRejectsInsufficientBattery() {
        Order order = createOrder(54, 2, 40.0);

        ElectricVehicleDriver electric =
                new ElectricVehicleDriver(
                        4, "Sara", true, 5.0, 4.8,
                        20.0, 100.0);

        assertFalse(electric.canDeliver(order));
    }

    @Test
    void automaticAssignmentSkipsIneligibleDrivers() {
        DriverRepository driverRepository = new DriverRepository();
        OrderRepository orderRepository = new OrderRepository();

        BicycleDriver bicycle = new BicycleDriver(
                1, "Ahmad", true, 1.0, 4.5);

        CarDriver car = new CarDriver(
                2, "Ali", true, 4.0, 4.9);

        driverRepository.save(bicycle);
        driverRepository.save(car);

        Order order = createOrder(55, 10, 8.0);
        orderRepository.save(order);

        DriverService service = new DriverService(
                driverRepository, orderRepository);

        service.assignDriverAutomatically(
                55, new NearestDriverStrategy());

        assertSame(car, order.getAssignedDriver());
        assertTrue(bicycle.isAvailable());
        assertFalse(car.isAvailable());
    }

    // Helper method for creating test orders
    private Order createOrder(int id, int quantity, double distanceKm) {

        User customer = new User(
                1, "customer1", "hash", "Customer");

        Restaurant restaurant = new Restaurant(
                1, "Pizza House", "Nablus", true);

        MenuItem pizza = new MenuItem(
                1, "Pizza", 25.0, 1);

        Order order = new Order(
                id,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, quantity)),
                "Nablus, Rafidia, Building 10"
        );

        order.setDeliveryDetails(distanceKm, 10.0);

        return order;
    }


    @Test
    void rejectsLargeOrderForBicycleWithoutChangingState() {
        DriverRepository drivers = new DriverRepository();
        OrderRepository orders = new OrderRepository();

        BicycleDriver bicycle =
                new BicycleDriver(1, "Ahmad", true, 2.0, 4.5);

        drivers.save(bicycle);

        Order order = createOrder(60, 4, 3.0);
        orders.save(order);

        DriverService service = new DriverService(drivers, orders);

        assertThrows(IllegalStateException.class,
                () -> service.assignDriver(60, 1));

        assertNull(order.getAssignedDriver());
        assertTrue(bicycle.isAvailable());
    }

    @Test
    void rejectsLargeOrderForMotorcycleWithoutChangingState() {
        DriverRepository drivers = new DriverRepository();
        OrderRepository orders = new OrderRepository();

        MotorcycleDriver motorcycle =
                new MotorcycleDriver(2, "Omar", true, 3.0, 4.7);

        drivers.save(motorcycle);

        Order order = createOrder(61, 9, 10.0);
        orders.save(order);

        DriverService service = new DriverService(drivers, orders);

        assertThrows(IllegalStateException.class,
                () -> service.assignDriver(61, 2));

        assertNull(order.getAssignedDriver());
        assertTrue(motorcycle.isAvailable());
    }

    @Test
    void rejectsElectricVehicleWithInsufficientBattery() {
        DriverRepository drivers = new DriverRepository();
        OrderRepository orders = new OrderRepository();

        ElectricVehicleDriver electric =
                new ElectricVehicleDriver(
                        4, "Sara", true, 5.0, 4.8,
                        20.0, 100.0);

        drivers.save(electric);

        Order order = createOrder(62, 2, 40.0);
        orders.save(order);

        DriverService service = new DriverService(drivers, orders);

        assertThrows(IllegalStateException.class,
                () -> service.assignDriver(62, 4));

        assertNull(order.getAssignedDriver());
        assertTrue(electric.isAvailable());
    }

    @Test
    void rejectsInvalidElectricVehicleBattery() {
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricVehicleDriver(
                        4, "Sara", true, 5.0, 4.8,
                        120.0, 100.0));

        assertThrows(IllegalArgumentException.class,
                () -> new ElectricVehicleDriver(
                        4, "Sara", true, 5.0, 4.8,
                        Double.NaN, 100.0));
    }

    @Test
    void automaticAssignmentRejectsWhenNoDriverIsEligible() {
        DriverRepository drivers = new DriverRepository();
        OrderRepository orders = new OrderRepository();

        BicycleDriver bicycle =
                new BicycleDriver(1, "Ahmad", true, 1.0, 4.5);

        drivers.save(bicycle);

        Order order = createOrder(63, 10, 8.0);
        orders.save(order);

        DriverService service = new DriverService(drivers, orders);

        assertThrows(IllegalStateException.class,
                () -> service.assignDriverAutomatically(
                        63, new NearestDriverStrategy()));

        assertNull(order.getAssignedDriver());
        assertTrue(bicycle.isAvailable());
    }

}
