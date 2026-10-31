
package com.smartfooddelivery.strategy;

import com.smartfooddelivery.model.Driver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DriverSelectionStrategyTest {

    @Test
    void nearestStrategyShouldSelectClosestDriver() {
        List<Driver> drivers = List.of(
                new Driver(1, "Ahmad", true, 5.0, 4.9),
                new Driver(2, "Omar", true, 2.0, 4.2),
                new Driver(3, "Sara", true, 3.0, 4.7)
        );

        DriverSelectionStrategy strategy = new NearestDriverStrategy();

        Driver selected = strategy.selectDriver(drivers);

        assertEquals(2, selected.getId());
    }

    @Test
    void highestRatedStrategyShouldSelectBestRating() {
        List<Driver> drivers = List.of(
                new Driver(1, "Ahmad", true, 5.0, 4.9),
                new Driver(2, "Omar", true, 2.0, 4.2),
                new Driver(3, "Sara", true, 3.0, 4.7)
        );

        DriverSelectionStrategy strategy = new HighestRatedDriverStrategy();

        Driver selected = strategy.selectDriver(drivers);

        assertEquals(1, selected.getId());
    }

    @Test
    void nearestStrategyShouldHandleEmptyList() {
        DriverSelectionStrategy strategy = new NearestDriverStrategy();

        assertNull(strategy.selectDriver(List.of()));
        assertNull(strategy.selectDriver(null));
    }

    @Test
    void highestRatedStrategyShouldHandleEmptyList() {
        DriverSelectionStrategy strategy = new HighestRatedDriverStrategy();

        assertNull(strategy.selectDriver(List.of()));
        assertNull(strategy.selectDriver(null));
    }
}
