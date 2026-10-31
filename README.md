# Smart-Food-Delivery
 Smart Food Delivery Management System

## Email notifications

Run commands from `food-delivery` (Java 17+ and Maven required).

1. Copy `.env.example` to `.env` in `food-delivery` and replace every placeholder with your SMTP provider settings. Environment variables override `.env`. Never commit credentials.
2. Use `SMTP_SECURITY=starttls` for STARTTLS (usually port 587) or `ssl` for implicit TLS (usually port 465). Use credentials/app passwords supplied by your provider.
3. Add the customer's email as an optional fifth column in `src/main/resources/users.txt`: `id,username,passwordHash,role,email`. Existing four-column records still load, but need an email before sending notifications. Programmatic callers can use `new User(id, username, passwordHash, role, email)`.
4. When constructing `OrderService`, supply the email observer:

```java
OrderService orders = new OrderService(
        orderRepository, restaurantRepository, deliveryPolicy,
        CustomerNotificationObserver.fromEnvironment());
```

Orders placed through that service notify the customer on subsequent status changes and driver assignments. Existing injected observers still work. This repository is a service library without an application entry point; the application must use the wiring above.

Build and run automated tests (these do not send real mail):

```powershell
mvn test
```

To explicitly send one real test email to your own address:

```powershell
mvn compile dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" com.smartfooddelivery.notification.EmailSmokeTest your-address@example.com
```

Successful execution means the SMTP server accepted the message; check the inbox/spam folder for delivery. Sending is synchronous with 10-second socket timeouts. Failures throw an exception; an order's in-memory state may already have changed before notification fails. Do not blindly repeat an order action to retry an email. There is no automatic retry or delivery queue.

Implementation uses [Eclipse Angus Mail](https://eclipse-ee4j.github.io/angus-mail/) and [dotenv-java](https://github.com/cdimascio/dotenv-java).
