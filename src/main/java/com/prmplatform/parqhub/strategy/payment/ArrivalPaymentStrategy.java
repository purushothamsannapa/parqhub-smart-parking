package com.prmplatform.parqhub.strategy.payment;

import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Concrete strategy for Arrival payment processing
 */
public class ArrivalPaymentStrategy implements PaymentStrategy {
    
    @Override
    public Payment processPayment(Booking booking, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setMethod(Payment.PaymentMethod.Arrival);
        payment.setStatus(Payment.PaymentStatus.Pending);
        payment.setTimestamp(LocalDateTime.now());
        return payment;
    }
    
    @Override
    public String getPaymentMethodName() {
        return "Arrival";
    }
    
    @Override
    public boolean isValidPaymentMethod(String method) {
        return "Arrival".equalsIgnoreCase(method);
    }
}