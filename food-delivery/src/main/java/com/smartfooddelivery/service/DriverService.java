
package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.repository.DriverRepository;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.strategy.DriverSelectionStrategy;

import java.util.List;

/**
 * Handles assigning drivers to delivery orders.
 * Supports manual and automatic driver assignment.
 */

public class DriverService {

    /**
     * Stores the driver repository.
     */

    private final DriverRepository driverRepository;

    /**
     * Stores the order repository.
     */

    private final OrderRepository orderRepository;

    /**
     * Creates a DriverService using the given repositories.
     *
     * @param driverRepository repository for drivers
     * @param orderRepository repository for orders
     */

    public DriverService(
            DriverRepository driverRepository,
            OrderRepository orderRepository) {

        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Assigns a driver to an order after checking if the driver
     * is available and can deliver the order.
     *
     * @param orderId the ID of the order
     * @param driverId the ID of the driver
     * @throws IllegalArgumentException if the order or driver is not found
     * @throws IllegalStateException if the driver cannot be assigned
     */

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

    /**
     * Automatically assigns a driver to an order using a selection strategy.
     *
     * @param orderId the ID of the order
     * @param strategy the strategy used to select a driver
     * @throws IllegalArgumentException if the order or strategy is invalid
     * @throws IllegalStateException if no suitable driver is available
     */

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
    /**
     * Checks if a driver can be assigned based on the order status.
     *
     * @param order the order to check
     * @throws IllegalStateException if the order status does not allow driver assignment
     */

    private void validateOrderStatus(Order order) {

        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {

            throw new IllegalStateException(
                    "Cannot assign driver in the current order status");
        }
    }
}
