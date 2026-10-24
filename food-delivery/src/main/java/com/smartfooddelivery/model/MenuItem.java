package com.smartfooddelivery.model;

public class MenuItem {

    private int id;
    private String name;
    private double price;
    private int restaurantId;
    private boolean available;

    public MenuItem(int id, String name, double price,
                    int restaurantId) {
        this(id, name, price, restaurantId, true);
    }

    public MenuItem(int id, String name, double price,
                    int restaurantId, boolean available) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.restaurantId = restaurantId;
        this.available = available;
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

    public boolean isAvailable() {
        return available;
    }
}