package com.smartfooddelivery.strategy;

import com.smartfooddelivery.model.Driver;
import java.util.List;

public class NearestDriverStrategy implements DriverSelectionStrategy {

    @Override
    public Driver selectDriver(List<Driver> availableDrivers) {

        if (availableDrivers == null || availableDrivers.isEmpty()) {
            return null;
        }

        Driver nearestDriver = availableDrivers.get(0);

        for (Driver driver : availableDrivers) {
            if (driver.getDistance() < nearestDriver.getDistance()) {
                nearestDriver = driver;
            }
        }

        return nearestDriver;
    }
}