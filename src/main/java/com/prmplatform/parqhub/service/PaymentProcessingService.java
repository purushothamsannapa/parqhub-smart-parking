package com.prmplatform.parqhub.service;

import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.Payment;
import com.prmplatform.parqhub.strategy.payment.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * Service class for payment processing using strategy pattern
 */
@Service
public class PaymentProcessingService {
    
    /**
     * Process payment using the specified payment method
     */
    public Payment processPayment(Booking booking, BigDecimal amount, String paymentMethod) {
        PaymentStrategy strategy = getStrategyForPaymentMethod(paymentMethod);
        PaymentContext context = new PaymentContext(strategy);
        return context.processPayment(booking, amount);
    }
    
    /**
     * Get the appropriate strategy for a given payment method
     */
    private PaymentStrategy getStrategyForPaymentMethod(String paymentMethod) {
        switch (paymentMethod.toLowerCase()) {
            case "card":
                return new CardPaymentStrategy();
            case "cash":
                return new CashPaymentStrategy();
            case "arrival":
                return new ArrivalPaymentStrategy();
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
    }
    
    /**
     * Validate if a payment method is supported
     */
    public boolean isValidPaymentMethod(String paymentMethod) {
        try {
            getStrategyForPaymentMethod(paymentMethod);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get all available payment methods
     */
    public String[] getAvailablePaymentMethods() {
        return new String[]{"Card", "Cash", "Arrival"};
    }
}