
package com.smartfooddelivery.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DriverTest {

    @Test
    void createValidDriverSuccessfully() {
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        assertEquals(1, driver.getId());
        assertEquals("Ahmad", driver.getName());
        assertTrue(driver.isAvailable());
        assertEquals(2.5, driver.getDistance());
        assertEquals(4.8, driver.getRating());
    }

    @Test
    void negativeDistanceShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, "Ahmad", true, -1, 4.8));
    }

    @Test
    void invalidRatingShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, "Ahmad", true, 2.5, 6.0));

        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, "Ahmad", true, 2.5, -1.0));
    }

    @Test
    void invalidIdShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Driver(0, "Ahmad", true, 2.5, 4.8));
    }

    @Test
    void invalidNameShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, null, true, 2.5, 4.8));

        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, "   ", true, 2.5, 4.8));
    }

    @Test
    void invalidSpecialNumbersShouldFail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, "Ahmad", true, Double.NaN, 4.8));

        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, "Ahmad", true, Double.POSITIVE_INFINITY, 4.8));

        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, "Ahmad", true, 2.5, Double.NaN));

        assertThrows(IllegalArgumentException.class,
                () -> new Driver(1, "Ahmad", true, 2.5, Double.POSITIVE_INFINITY));
    }

    @Test
    void updateDriverAvailabilitySuccessfully() {
        Driver driver = new Driver(1, "Ahmad", true, 2.5, 4.8);

        driver.setAvailable(false);
        assertFalse(driver.isAvailable());

        driver.setAvailable(true);
        assertTrue(driver.isAvailable());
    }
}
