package com.smartfooddelivery.repository;
import com.smartfooddelivery.model.Driver;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
public class DriverRepository {

    private final Map<Integer, Driver> drivers = new HashMap<>();

    public void save(Driver driver) {

        if (driver == null) {
            throw new IllegalArgumentException("Driver is required");
        }

        if (drivers.containsKey(driver.getId())) {
            throw new IllegalArgumentException(
                    "A driver with this ID already exists"
            );
        }

        drivers.put(driver.getId(), driver);
    }

    public Driver findById(int id) {
        return drivers.get(id);
    }

    public List<Driver> findAvailableDrivers() {
        List<Driver> availableDrivers = new ArrayList<>();

        for (Driver driver : drivers.values()) {
            if (driver.isAvailable()) {
                availableDrivers.add(driver);
            }
        }

        return availableDrivers;
    }

}
