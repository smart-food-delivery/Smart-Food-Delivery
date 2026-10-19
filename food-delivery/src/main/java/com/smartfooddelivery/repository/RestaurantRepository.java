package com.smartfooddelivery.repository;
import com.smartfooddelivery.model.Restaurant;
import java.util.List;
import java.util.ArrayList;
public class RestaurantRepository {
    private List<Restaurant> restaurants = new ArrayList<>();
    public RestaurantRepository() {
        restaurants.add(new Restaurant(1, "Pizza House", "Nablus", true));
        restaurants.add(new Restaurant(2, "Burger Town", "Ramallah", true));
        restaurants.add(new Restaurant(3, "Pasta Corner", "Jenin", false));
    }
    public List<Restaurant> findAll() {
        return restaurants;
    }
}
