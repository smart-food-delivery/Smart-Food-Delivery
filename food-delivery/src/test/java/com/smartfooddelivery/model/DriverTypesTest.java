
package com.smartfooddelivery.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DriverTypesTest {

    @Test
    void supportsBicycleDriver() {
        Driver driver = new BicycleDriver(
                1, "Ahmad", true, 2.0, 4.5
        );

        assertInstanceOf(BicycleDriver.class, driver);
        assertEquals(1, driver.getId());
        assertTrue(driver.isAvailable());
    }

    @Test
    void supportsMotorcycleDriver() {
        Driver driver = new MotorcycleDriver(
                2, "Omar", true, 4.0, 4.7
        );

        assertInstanceOf(MotorcycleDriver.class, driver);
        assertEquals("Omar", driver.getName());
        assertEquals(4.0, driver.getDistance(), 0.001);
    }

    @Test
    void supportsCarDriver() {
        Driver driver = new CarDriver(
                3, "Ali", true, 3.0, 4.9
        );

        assertInstanceOf(CarDriver.class, driver);
        assertEquals(4.9, driver.getRating(), 0.001);
    }

    @Test
    void supportsElectricVehicleDriver() {
        Driver driver = new ElectricVehicleDriver(
                4, "Sara", true, 5.0, 4.8
        );

        assertInstanceOf(ElectricVehicleDriver.class, driver);
        assertEquals(4, driver.getId());
    }

    @Test
    void preservesDriverValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new BicycleDriver(
                        1, "Ahmad", true, -2.0, 4.5
                ));

        assertThrows(IllegalArgumentException.class,
                () -> new CarDriver(
                        2, "Ali", true, 3.0, 6.0
                ));
    }


    @Test
    void bicycleCanDeliverWithinAllowedDistance() {

        BicycleDriver bicycle = new BicycleDriver(
                1, "Ahmad", true, 2.0, 4.5
        );

        assertTrue(bicycle.canDeliver(3.0));
        assertTrue(bicycle.canDeliver(5.0));
    }

    @Test
    void bicycleCannotDeliverBeyondMaximumDistance() {

        BicycleDriver bicycle = new BicycleDriver(
                1, "Ahmad", true, 2.0, 4.5
        );

        assertFalse(bicycle.canDeliver(8.0));
        assertFalse(bicycle.canDeliver(5.1));
    }

    @Test
    void bicycleRejectsInvalidDeliveryDistances() {

        BicycleDriver bicycle = new BicycleDriver(
                1, "Ahmad", true, 2.0, 4.5
        );

        assertFalse(bicycle.canDeliver(-1.0));
        assertFalse(bicycle.canDeliver(Double.NaN));
        assertFalse(bicycle.canDeliver(Double.POSITIVE_INFINITY));
    }


    @Test
    void motorcycleCanDeliverWithinAllowedDistance() {

        MotorcycleDriver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 3.0, 4.7
        );

        assertTrue(motorcycle.canDeliver(8.0));
        assertTrue(motorcycle.canDeliver(15.0));
    }

    @Test
    void motorcycleCannotDeliverBeyondMaximumDistance() {

        MotorcycleDriver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 3.0, 4.7
        );

        assertFalse(motorcycle.canDeliver(18.0));
        assertFalse(motorcycle.canDeliver(15.1));
    }

    @Test
    void motorcycleRejectsInvalidDeliveryDistances() {

        MotorcycleDriver motorcycle = new MotorcycleDriver(
                2, "Omar", true, 3.0, 4.7
        );

        assertFalse(motorcycle.canDeliver(-2.0));
        assertFalse(motorcycle.canDeliver(Double.NaN));
        assertFalse(motorcycle.canDeliver(Double.POSITIVE_INFINITY));
    }



}
