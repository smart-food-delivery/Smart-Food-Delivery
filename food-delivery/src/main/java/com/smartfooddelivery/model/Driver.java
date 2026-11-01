package com.smartfooddelivery.model;

public class Driver {
    private final int id;
    private final String name;
    private boolean available;
    private double distance;
    private double rating;

    public Driver(int id, String name, boolean available,
                  double distance, double rating) {

        if (distance < 0 || Double.isNaN(distance)
                || Double.isInfinite(distance)) {
            throw new IllegalArgumentException("Invalid driver distance");
        }

        if (rating < 0 || rating > 5
                || Double.isNaN(rating)
                || Double.isInfinite(rating)) {
            throw new IllegalArgumentException("Invalid driver rating");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid driver ID");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Driver name is required");
        }

        this.id = id;
        this.name = name;
        this.available = available;
        this.distance = distance;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public double getDistance() {
        return distance;
    }

    public double getRating() {
        return rating;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }



    public boolean canDeliver(Order order) {
        return order != null;
    }

}
