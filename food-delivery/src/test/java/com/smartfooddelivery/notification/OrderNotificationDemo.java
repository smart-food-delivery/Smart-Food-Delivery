package com.smartfooddelivery.notification;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.model.Driver;
import com.smartfooddelivery.model.MenuItem;
import com.smartfooddelivery.model.Order;
import com.smartfooddelivery.model.OrderItem;
import com.smartfooddelivery.model.User;
import com.smartfooddelivery.repository.DriverRepository;
import com.smartfooddelivery.repository.OrderRepository;
import com.smartfooddelivery.repository.RestaurantRepository;
import com.smartfooddelivery.service.DeliveryPolicy;
import com.smartfooddelivery.service.DriverService;
import com.smartfooddelivery.service.OrderService;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import com.smartfooddelivery.service.DeliveryCostService;

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

        OrderRepository orderRepository = new OrderRepository();
        OrderService orderService = new OrderService(
                orderRepository,
                new RestaurantRepository(),
                new DeliveryPolicy(30.0, Set.of("Nablus")),
                CustomerNotificationObserver.fromEnvironment(),
                new DeliveryCostService(2.0)
        );

        MenuItem pizza = new MenuItem(1, "Pizza", 25.0, 1);


        Order order = orderService.placeOrderWithDelivery(
                10,
                customer,
                1,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10",
                5.0
        );



        System.out.println("Order ID: " + order.getId());
        System.out.println("Subtotal: " + order.getSubtotal());
        System.out.println("Delivery distance: "
                + order.getDeliveryDistanceKm() + " km");
        System.out.println("Delivery fee: " + order.getDeliveryFee());
        System.out.println("Final total: " + order.getTotal());
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

        System.out.print(
                "Type YES to continue delivery and send five more real emails: "
        );

        if (!scanner.nextLine().trim().equalsIgnoreCase("YES")) {
            System.out.println("Delivery demo stopped.");
            return;
        }

        DriverRepository driverRepository = new DriverRepository();

        Driver driver = new Driver(
                1, "Ahmad", true, 2.0, 4.8
        );
        driverRepository.save(driver);

        DriverService driverService = new DriverService(
                driverRepository, orderRepository
        );

        driverService.assignDriver(order.getId(), driver.getId());
        System.out.println("Driver assigned: " + driver.getName());

        order.changeStatus(OrderStatus.PREPARING);
        order.changeStatus(OrderStatus.READY);
        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.changeStatus(OrderStatus.DELIVERED);

        System.out.println("Final order status: " + order.getStatus());
        System.out.println(
                "Check your inbox for the delivery notifications."
        );
    }
}