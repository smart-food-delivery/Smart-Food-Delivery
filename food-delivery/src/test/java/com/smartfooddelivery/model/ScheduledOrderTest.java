package com.smartfooddelivery.model;

import com.smartfooddelivery.domain.OrderStatus;
import com.smartfooddelivery.domain.PaymentStatus;
import com.smartfooddelivery.strategy.PromotionalPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduledOrderTest {

    private static final Instant NOW =
            Instant.parse("2026-11-01T10:00:00Z");

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(NOW);
    }

    private ScheduledOrder createOrder(
            Instant deliveryTime, Clock suppliedClock) {

        User customer =
                new User(1, "customer", "hash", "Customer");

        Restaurant restaurant =
                new Restaurant(1, "Pizza House", "Nablus", true);

        MenuItem pizza =
                new MenuItem(1, "Pizza", 25.0, 1);

        return new ScheduledOrder(
                10,
                customer,
                restaurant,
                List.of(new OrderItem(pizza, 2)),
                "Nablus, Rafidia, Building 10",
                deliveryTime,
                suppliedClock
        );
    }

    @Test
    void acceptsAndStoresFutureDeliveryTime() {
        Instant futureTime = NOW.plusSeconds(3600);

        ScheduledOrder order = createOrder(futureTime, clock);

        assertEquals(futureTime, order.getScheduledDeliveryTime());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());

        verify(clock).instant();
    }

    @Test
    void rejectsPastDeliveryTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createOrder(NOW.minusSeconds(1), clock)
        );
    }

    @Test
    void rejectsDeliveryTimeEqualToNow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createOrder(NOW, clock)
        );
    }

    @Test
    void acceptsTimeJustAfterNow() {
        Instant futureTime = NOW.plusNanos(1);

        ScheduledOrder order = createOrder(futureTime, clock);

        assertEquals(futureTime, order.getScheduledDeliveryTime());
    }

    @Test
    void rejectsMissingDeliveryTime() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createOrder(null, clock)
        );
    }

    @Test
    void rejectsMissingClock() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createOrder(NOW.plusSeconds(3600), null)
        );
    }

    @Test
    void calculatesTotalWithoutAdditionalSchedulingFee() {
        Order order = createOrder(NOW.plusSeconds(3600), clock);

        order.setDeliveryDetails(5.0, 10.0);

        assertEquals(60.0, order.getTotal(), 0.001);
    }

    @Test
    void supportsExistingPricingStrategies() {
        Order order = createOrder(NOW.plusSeconds(3600), clock);

        order.setDeliveryDetails(5.0, 10.0);
        order.setPricingStrategy(
                new PromotionalPricingStrategy(10.0)
        );

        assertEquals(55.0, order.getTotal(), 0.001);
    }
}