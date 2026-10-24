package com.smartfooddelivery.service;

import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class RestaurantServiceTest {
    @Test
    void testGetAvailableRestaurants() {

        RestaurantRepository mockRepository =
                mock(RestaurantRepository.class);
        Restaurant restaurant1 =
                new Restaurant(1, "Pizza House", "Nablus", true);

        Restaurant restaurant2 =
                new Restaurant(2, "Burger Town", "Ramallah", false);

        when(mockRepository.findAll())
                .thenReturn(List.of(restaurant1, restaurant2));
        RestaurantService service = new RestaurantService(mockRepository);

        List<Restaurant> result = service.getAvailableRestaurants();

        assertEquals(1, result.size());
        assertEquals("Pizza House", result.get(0).getName());

    }
    @Test
    void testNoAvailableRestaurants() {
        RestaurantRepository mockRepository =
                mock(RestaurantRepository.class);

        Restaurant restaurant1 =
                new Restaurant(1, "Pizza House", "Nablus", false);
        when(mockRepository.findAll())
                .thenReturn(List.of(restaurant1));
        RestaurantService service = new RestaurantService(mockRepository);

        List<Restaurant> result = service.getAvailableRestaurants();
        assertTrue(result.isEmpty());


    }
    @Test
    void testEmptyRestaurantList() {
        RestaurantRepository mockRepository =
                mock(RestaurantRepository.class);

        when(mockRepository.findAll())
                .thenReturn(List.of());
        RestaurantService service = new RestaurantService(mockRepository);

        List<Restaurant> result = service.getAvailableRestaurants();

        assertTrue(result.isEmpty());

    }







}
