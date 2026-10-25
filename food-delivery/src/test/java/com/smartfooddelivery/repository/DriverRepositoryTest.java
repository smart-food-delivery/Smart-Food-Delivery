package com.smartfooddelivery.repository;
import com.smartfooddelivery.model.Driver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
public class DriverRepositoryTest {

    @Test
    void saveDriverSuccessfully() {

        DriverRepository repository = new DriverRepository();

        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        repository.save(driver);

        assertEquals(driver, repository.findById(1));
    }

    @Test
    void saveDriverWithDuplicateIdShouldFail() {

        DriverRepository repository = new DriverRepository();

        Driver firstDriver = new Driver(1, "Ahmad", true, 2.5, 4.8);
        Driver secondDriver = new Driver(1, "Omar", true, 3.0, 4.5);

        repository.save(firstDriver);

        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(secondDriver);
        });

        assertEquals(firstDriver, repository.findById(1));
    }

    @Test
    void saveNullDriverShouldFail() {

        DriverRepository repository = new DriverRepository();

        assertThrows(IllegalArgumentException.class, () -> {
            repository.save(null);
        });
    }

    @Test
    void findAvailableDriversShouldReturnOnlyAvailableDrivers() {

        DriverRepository repository = new DriverRepository();

        Driver ahmad = new Driver(1, "Ahmad", true, 2.5, 4.8);
        Driver sara = new Driver(2, "Sara", false, 3.0, 4.5);
        Driver omar = new Driver(3, "Omar", true, 1.5, 4.9);

        repository.save(ahmad);
        repository.save(sara);
        repository.save(omar);

        List<Driver> availableDrivers = repository.findAvailableDrivers();

        assertEquals(2, availableDrivers.size());
        assertTrue(availableDrivers.contains(ahmad));
        assertTrue(availableDrivers.contains(omar));
        assertFalse(availableDrivers.contains(sara));
    }

    @Test
    void findNonExistingDriverShouldReturnNull() {

        DriverRepository repository = new DriverRepository();

        assertNull(repository.findById(99));
    }

    @Test
    void findAvailableDriversWhenNoneAvailableShouldReturnEmptyList() {

        DriverRepository repository = new DriverRepository();

        repository.save(new Driver(1, "Ahmad", false, 2.5, 4.8));
        repository.save(new Driver(2, "Omar", false, 3.0, 4.5));

        assertTrue(repository.findAvailableDrivers().isEmpty());
    }

}
