package com.prmplatform.parqhub.strategy.payment;

import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.Payment;
import java.math.BigDecimal;

/**
 * Strategy interface for payment processing
 */
public interface PaymentStrategy {
    Payment processPayment(Booking booking, BigDecimal amount);
    String getPaymentMethodName();
    boolean isValidPaymentMethod(String method);
}