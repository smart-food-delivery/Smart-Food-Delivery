
package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.repository.DriverRepository;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.strategy.DriverSelectionStrategy;

import java.util.List;

public class DriverService {

    private final DriverRepository driverRepository;
    private final OrderRepository orderRepository;

    public DriverService(
            DriverRepository driverRepository,
            OrderRepository orderRepository) {

        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
    }

    public void assignDriver(int orderId, int driverId) {

        Order order = orderRepository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        Driver driver = driverRepository.findById(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver not found");
        }

        if (!driver.isAvailable()) {
            throw new IllegalStateException(
                    "Driver is not available");
        }

        validateOrderStatus(order);

        // Polymorphism: each driver type applies its own rules
        if (!driver.canDeliver(order)) {
            throw new IllegalStateException(
                    "Driver is not eligible for this order");
        }

        // Assign the driver only after all validations pass
        order.assignDriver(driver);

        // Also supports tests that use mocked orders
        driver.setAvailable(false);
    }

    public void assignDriverAutomatically(
            int orderId,
            DriverSelectionStrategy strategy) {

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Selection strategy is required");
        }

        List<Driver> availableDrivers =
                driverRepository.findAvailableDrivers();

        if (availableDrivers.isEmpty()) {
            throw new IllegalStateException(
                    "No available drivers");
        }

        Order order = orderRepository.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found");
        }

        validateOrderStatus(order);

        // Only eligible drivers participate in selection
        List<Driver> eligibleDrivers = availableDrivers.stream()
                .filter(driver -> driver.canDeliver(order))
                .toList();

        Driver selectedDriver =
                strategy.selectDriver(eligibleDrivers);

        if (selectedDriver == null) {
            throw new IllegalStateException(
                    "No eligible drivers available");
        }

        assignDriver(orderId, selectedDriver.getId());
    }

    private void validateOrderStatus(Order order) {

        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {

            throw new IllegalStateException(
                    "Cannot assign driver in the current order status");
        }
    }
}
