package com.smartfooddelivery.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ExpressOrder extends Order {

    private final double expressFee;

    public ExpressOrder(int id, User customer, Restaurant restaurant,
                        List<OrderItem> items, String deliveryAddress,
                        double expressFee) {

        super(id, customer, restaurant, items, deliveryAddress);

        if (!Double.isFinite(expressFee) || expressFee <= 0) {
            throw new IllegalArgumentException(
                    "Express fee must be finite and positive"
            );
        }

        try {
            this.expressFee = BigDecimal.valueOf(expressFee)
                    .setScale(2, RoundingMode.UNNECESSARY)
                    .doubleValue();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Express fee must have at most two decimal places",
                    exception
            );
        }
    }

    public double getExpressFee() {
        return expressFee;
    }

    @Override
    public double getTotal() {
        double total = BigDecimal.valueOf(super.getTotal())
                .add(BigDecimal.valueOf(expressFee))
                .doubleValue();

        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException("Total is too large");
        }

        return total;
    }
}