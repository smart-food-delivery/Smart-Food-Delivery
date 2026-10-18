package com.smartfooddelivery.domain;

/**
 * Checks whether cancellation is allowed in this status.
 *
 * <p>Currently, only PENDING allows cancellation.
 * Cancellation in CONFIRMED is temporarily disallowed
 * pending clarification of the requirement.</p>
 *
 * @return true if this status is PENDING; false otherwise
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;
    public boolean canBeCancelled() {
        return this == PENDING;
    }
}