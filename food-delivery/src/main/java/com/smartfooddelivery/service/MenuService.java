package com.smartfooddelivery.service;
import com.smartfooddelivery.repository.MenuRepository;
import com.smartfooddelivery.repository.RestaurantRepository;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Restaurant;
import java.util.List;
import java.util.Collections;

public class MenuService {
    private MenuRepository menuRepository;
    private RestaurantRepository restaurantRepository;
    public MenuService(MenuRepository menuRepository,
                       RestaurantRepository restaurantRepository) {
        this.menuRepository = menuRepository;
        this.restaurantRepository = restaurantRepository;
    }
    public List<MenuItem> getRestaurantMenu(int restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId);

        if (restaurant == null || !restaurant.isAvailable()) {
            return Collections.emptyList();
        }

        return menuRepository.findByRestaurantId(restaurantId);
    }

}
