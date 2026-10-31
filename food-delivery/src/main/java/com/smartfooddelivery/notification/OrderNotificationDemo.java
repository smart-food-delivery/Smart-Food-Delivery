package com.smartfooddelivery.notification;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.repository.RestaurantRepository;
import com.smartfooddelivery.service.DeliveryPolicy;
import com.smartfooddelivery.service.OrderService;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class OrderNotificationDemo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();

        if (email.isBlank()) {
            System.out.println("No email entered. Demo stopped.");
            return;
        }

        User customer = new User(
                1, "demo-customer", "demo-hash", "Customer", email
        );

        OrderService orderService = new OrderService(
                new OrderRepository(),
                new RestaurantRepository(),
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                CustomerNotificationObserver.fromEnvironment()
        );

        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);

        Order order = orderService.placeOrder(
                10,
                customer,
                1,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10"
        );

        System.out.println("Order ID: " + order.getId());
        System.out.println("Subtotal: " + order.getSubtotal());
        System.out.println("Current status: " + order.getStatus());

        System.out.print(
                "Type YES to confirm the order and send one real email: "
        );

        if (!scanner.nextLine().trim().equalsIgnoreCase("YES")) {
            System.out.println("Demo stopped. No status-change email sent.");
            return;
        }

        order.changeStatus(OrderStatus.CONFIRMED);

        System.out.println("New status: " + order.getStatus());
        System.out.println(
                "Notification call completed. Check your email inbox."
        );
    }
}