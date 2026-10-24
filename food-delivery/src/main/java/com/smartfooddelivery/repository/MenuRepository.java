package com.smartfooddelivery.repository;
import com.smartfooddelivery.model.MenuItem;
import java.util.ArrayList;
import java.util.List;
public class MenuRepository {
    private List<MenuItem> menuItems = new ArrayList<>();
    public MenuRepository() {
        menuItems.add(new MenuItem(1, "Margherita Pizza", 25.0, 1));
        menuItems.add(new MenuItem(2, "Pepperoni Pizza", 30.0, 1));
        menuItems.add(new MenuItem(3, "Chicken Burger", 20.0, 2));
        menuItems.add(new MenuItem(4, "French Fries", 10.0, 2));
        menuItems.add(new MenuItem(5, "Pasta Alfredo", 28.0, 3));
    }
    public List<MenuItem> findByRestaurantId(int restaurantId) {
        List<MenuItem> result = new ArrayList<>();

        for (MenuItem item : menuItems) {
            if (item.getRestaurantId() == restaurantId) {
                result.add(item);
            }
        }

        return result;
    }





}
