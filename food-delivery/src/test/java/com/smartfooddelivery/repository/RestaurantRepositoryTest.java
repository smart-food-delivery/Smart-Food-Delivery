package com.smartfooddelivery.repository;

import com.smartfooddelivery.model.Restaurant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RestaurantRepositoryTest {

    @Test
    void returnsAllSeededRestaurantsIncludingClosedOnes() {
        RestaurantRepository repository = new RestaurantRepository();

        List<Restaurant> restaurants = repository.findAll();

        assertEquals(3, restaurants.size());
        assertTrue(restaurants.stream()
                .anyMatch(restaurant -> !restaurant.isAvailable()));
    }

    @Test
    void findsRestaurantById() {
        RestaurantRepository repository = new RestaurantRepository();

        Restaurant restaurant = repository.findById(2);

        assertNotNull(restaurant);
        assertEquals(2, restaurant.getId());
        assertEquals("Burger Town", restaurant.getName());
    }

    @Test
    void returnsNullForUnknownRestaurantId() {
        RestaurantRepository repository = new RestaurantRepository();

        assertNull(repository.findById(999));
    }
}