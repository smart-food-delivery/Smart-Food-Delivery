package com.smartfooddelivery.strategy;

import com.smartfooddelivery.model.Driver;
import java.util.List;

public interface DriverSelectionStrategy {

    Driver selectDriver(List<Driver> availableDrivers);

}