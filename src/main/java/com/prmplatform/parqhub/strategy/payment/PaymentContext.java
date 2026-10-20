package com.prmplatform.parqhub.strategy.payment;

import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.Payment;
import java.math.BigDecimal;

/**
 * Context class for payment strategies
 */
public class PaymentContext {
    private PaymentStrategy paymentStrategy;
    
    public PaymentContext(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    
    public Payment processPayment(Booking booking, BigDecimal amount) {
        return paymentStrategy.processPayment(booking, amount);
    }
    
    public String getPaymentMethodName() {
        return paymentStrategy.getPaymentMethodName();
    }
    
    public boolean isValidPaymentMethod(String method) {
        return paymentStrategy.isValidPaymentMethod(method);
    }
}