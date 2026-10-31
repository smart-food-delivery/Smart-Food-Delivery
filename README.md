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

Orders placed through that service notify the customer on subsequent status changes and driver assignments.

The project currently provides service classes and two manual console entry points:
- `EmailSmokeTest`: sends one real test email.
- `OrderNotificationDemo`: demonstrates order confirmation, driver assignment, and delivery status notifications.

A complete customer-facing application is not implemented yet.
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
## Build, tests, and coverage

From the repository root (`software`), run:

```powershell
mvn -f food-delivery/pom.xml clean verify
```

The JaCoCo coverage report is generated at:

```text
food-delivery/target/site/jacoco/index.html
```

Automated email tests use mocks and do not send real emails.
Manual runs are not included in this Maven coverage report.

## Manual order notification demo

In IntelliJ, run:

```text
com.smartfooddelivery.notification.OrderNotificationDemo
```

Set the working directory to the `food-delivery` folder containing `.env`,
or provide the SMTP settings through the run configuration's environment variables.

Enter your own email address and type `YES` at both confirmation prompts.
A successful complete run sends six notifications:
confirmation, driver assignment, preparing, ready, out for delivery, and delivered.

## Driver assignment and notification failure

The order records its assigned driver and marks that driver unavailable
before notifying observers. If email sending fails, the assignment remains
in place and the driver remains busy. The notification exception is
propagated to the caller; the assignment is not rolled back.


## Pricing strategies — US4.3

Orders use normal pricing by default:

- Normal pricing: food subtotal + delivery fee.
- Promotional pricing: applies a configurable percentage discount to
  the food subtotal, then adds the full delivery fee.
- Peak-hour pricing: applies a configurable percentage surcharge to
  the delivery fee, then adds the food subtotal.

These are implementation choices; the project requirements do not
specify the discount or surcharge percentages.

Pricing strategies can only be changed while an order is PENDING
and unpaid. Delivery details follow the same restriction.

Final totals are rounded to two decimal places using HALF_UP.
Peak-hour pricing is selected explicitly; time-based selection is
not automatic.

Example, before confirming the order:

```java
order.setPricingStrategy(new PromotionalPricingStrategy(10.0));
double total = order.getTotal();
```

## Cash payments — US4.4

Cash payment is processed through OrderPaymentService using the
order's final total, including pricing adjustments and delivery.

Our cash-on-delivery rule allows payment while the order is
OUT_FOR_DELIVERY, before it is marked DELIVERED.

- New orders start with payment status UNPAID.
- Successful payment changes the payment status to PAID.
- Insufficient or invalid cash is rejected and leaves the order UNPAID.
- Duplicate payments are rejected.
- The service returns the customer's change.
- Amounts must be finite, non-negative, and represent whole cents.
- A zero-value bill is supported.

Payment does not automatically change the delivery status.

Example:

```java
OrderPaymentService payments = new OrderPaymentService(
        orderRepository, new CashPaymentService()
);

double change = payments.payOrder(orderId, amountReceived);
```

Automated tests use a mocked PaymentService to check payment success
and failure, and also test integration with the real CashPaymentService.