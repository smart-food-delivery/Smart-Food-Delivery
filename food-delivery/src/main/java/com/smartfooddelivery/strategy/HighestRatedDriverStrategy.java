package com.smartfooddelivery.strategy;

import com.smartfooddelivery.model.Driver;
import java.util.List;

public class HighestRatedDriverStrategy implements DriverSelectionStrategy {

    @Override
    public Driver selectDriver(List<Driver> availableDrivers) {

        if (availableDrivers == null || availableDrivers.isEmpty()) {
            return null;
        }

        Driver highestRatedDriver = availableDrivers.get(0);

        for (Driver driver : availableDrivers) {
            if (driver.getRating() > highestRatedDriver.getRating()) {
                highestRatedDriver = driver;
            }
        }

        return highestRatedDriver;
    }
}