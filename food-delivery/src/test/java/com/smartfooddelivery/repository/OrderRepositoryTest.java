package com.smartfooddelivery.repository;

import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.Restaurant;
import com.smartfooddelivery.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderRepositoryTest {

    private Order createOrder(int id) {
        User customer = new User(1, "customer1", "hash", "Customer");
        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);
        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        return new Order(
                id, customer, restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );
    }

    @Test
    void savesAndFindsOrderById() {
        OrderRepository repository = new OrderRepository();
        Order order = createOrder(1);

        repository.save(order);

        assertSame(order, repository.findById(1));
    }

    @Test
    void returnsNullForUnknownId() {
        OrderRepository repository = new OrderRepository();
        repository.save(createOrder(1));

        assertNull(repository.findById(99));
    }

    @Test
    void rejectsDuplicateIdWithoutReplacingOriginalOrder() {
        OrderRepository repository = new OrderRepository();
        Order original = createOrder(1);
        Order duplicate = createOrder(1);

        repository.save(original);

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(duplicate));

        assertSame(original, repository.findById(1));
    }

    @Test
    void rejectsNullOrder() {
        OrderRepository repository = new OrderRepository();

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(null));
    }

    @Test
    void storesOrdersWithDifferentIdsSeparately() {
        OrderRepository repository = new OrderRepository();
        Order first = createOrder(1);
        Order second = createOrder(2);

        repository.save(first);
        repository.save(second);

        assertSame(first, repository.findById(1));
        assertSame(second, repository.findById(2));
    }
}