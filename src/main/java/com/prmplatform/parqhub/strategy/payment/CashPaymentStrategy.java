package com.prmplatform.parqhub.strategy.payment;

import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Concrete strategy for Cash payment processing
 */
public class CashPaymentStrategy implements PaymentStrategy {
    
    @Override
    public Payment processPayment(Booking booking, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setMethod(Payment.PaymentMethod.Cash);
        payment.setStatus(Payment.PaymentStatus.Completed);
        payment.setTimestamp(LocalDateTime.now());
        return payment;
    }
    
    @Override
    public String getPaymentMethodName() {
        return "Cash";
    }
    
    @Override
    public boolean isValidPaymentMethod(String method) {
        return "Cash".equalsIgnoreCase(method);
    }
}