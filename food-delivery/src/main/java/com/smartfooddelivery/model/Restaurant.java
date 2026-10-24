package com.smartfooddelivery.model;

public class Restaurant {
    private int id;
    private String name;
    private String address;
    private boolean available;
    public Restaurant(int id, String name, String address, boolean available) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.available = available;
    }
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isAvailable() {
        return available;
    }
}
