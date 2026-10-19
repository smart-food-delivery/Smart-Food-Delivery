package com.smartfooddelivery.service;
import com.smartfooddelivery.repository.RestaurantRepository;
import com.smartfooddelivery.repository.RestaurantRepository;
import com.smartfooddelivery.model.Restaurant;
import java.util.List;
import java.util.ArrayList;public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }
    public List<Restaurant> getAvailableRestaurants() {
        List<Restaurant> availableRestaurants = new ArrayList<>();

        for (Restaurant restaurant : restaurantRepository.findAll()) {
            if (restaurant.isAvailable()) {
                availableRestaurants.add(restaurant);
            }
        }

        return availableRestaurants;
    }
}
