package com.smartfooddelivery.service;

public interface PaymentService {

    /**
     * Processes payment and returns the customer's change.
     *
     * @param amountDue final amount required
     * @param amountReceived amount received from the customer
     * @return change to return to the customer
     */
    double processPayment(double amountDue, double amountReceived);
}