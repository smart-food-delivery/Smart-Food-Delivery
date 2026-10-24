package com.smartfooddelivery.model;

public class MenuItem {
    private int id;
    private String name;
    private double price;
    private int restaurantId;
    public MenuItem(int id, String name, double price, int restaurantId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.restaurantId = restaurantId;
    }
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getRestaurantId() {
        return restaurantId;
    }
}
