package com.smartfooddelivery.service;

import com.smartfooddelivery.repository.DriverRepository;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.strategy.DriverSelectionStrategy;
public class DriverService {

    private final DriverRepository driverRepository;
    private final OrderRepository orderRepository;

    public DriverService(DriverRepository driverRepository,
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
            throw new IllegalStateException("Driver is not available");
        }
        if (order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {

            throw new IllegalStateException(
                    "Cannot assign driver in the current order status"
            );
        }
        order.assignDriver(driver);
        driver.setAvailable(false);
    }

    public void assignDriverAutomatically(
            int orderId,
            DriverSelectionStrategy strategy) {

        if (strategy == null) {
            throw new IllegalArgumentException("Selection strategy is required");
        }

        Driver selectedDriver = strategy.selectDriver(
                driverRepository.findAvailableDrivers()
        );

        if (selectedDriver == null) {
            throw new IllegalStateException("No available drivers");
        }

        assignDriver(orderId, selectedDriver.getId());
    }

}