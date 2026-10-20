package com.prmplatform.parqhub.strategy.payment;

import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Concrete strategy for Card payment processing
 */
public class CardPaymentStrategy implements PaymentStrategy {
    
    @Override
    public Payment processPayment(Booking booking, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setMethod(Payment.PaymentMethod.Card);
        payment.setStatus(Payment.PaymentStatus.Completed);
        payment.setTimestamp(LocalDateTime.now());
        return payment;
    }
    
    @Override
    public String getPaymentMethodName() {
        return "Card";
    }
    
    @Override
    public boolean isValidPaymentMethod(String method) {
        return "Card".equalsIgnoreCase(method);
    }
}