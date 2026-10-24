package com.smartfooddelivery.service;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.repository.MenuRepository;
import com.smartfooddelivery.repository.RestaurantRepository;
import com.smartfooddelivery.service.MenuService;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class MenuServiceTest {
    @Test
    void shouldReturnMenuForAvailableRestaurant() {

        MenuRepository menuRepository = mock(MenuRepository.class);
        RestaurantRepository restaurantRepository = mock(RestaurantRepository.class);

        MenuService menuService =
                new MenuService(menuRepository, restaurantRepository);

        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        List<MenuItem> menu = List.of(
                new MenuItem(1, "Margherita Pizza", 25.0, 1),
                new MenuItem(2, "Pepperoni Pizza", 30.0, 1)
        );

        when(restaurantRepository.findById(1)).thenReturn(restaurant);
        when(menuRepository.findByRestaurantId(1)).thenReturn(menu);

        List<MenuItem> result = menuService.getRestaurantMenu(1);

        assertEquals(2, result.size());
        assertEquals("Margherita Pizza", result.get(0).getName());
        assertEquals(25.0, result.get(0).getPrice());
    }

    @Test
    void shouldReturnEmptyMenuForUnavailableRestaurant() {

        MenuRepository menuRepository = mock(MenuRepository.class);
        RestaurantRepository restaurantRepository = mock(RestaurantRepository.class);

        MenuService menuService =
                new MenuService(menuRepository, restaurantRepository);

        Restaurant restaurant =
                new Restaurant(3, "Pasta Corner", "Jenin", false);

        when(restaurantRepository.findById(3)).thenReturn(restaurant);

        List<MenuItem> result = menuService.getRestaurantMenu(3);

        assertTrue(result.isEmpty());
        verify(menuRepository, never()).findByRestaurantId(3);
    }

    @Test
    void shouldReturnEmptyMenuForNonExistingRestaurant() {

        MenuRepository menuRepository = mock(MenuRepository.class);
        RestaurantRepository restaurantRepository = mock(RestaurantRepository.class);

        MenuService menuService =
                new MenuService(menuRepository, restaurantRepository);

        when(restaurantRepository.findById(99)).thenReturn(null);

        List<MenuItem> result = menuService.getRestaurantMenu(99);

        assertTrue(result.isEmpty());
        verify(menuRepository, never()).findByRestaurantId(99);
    }


}
