package org.example.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DefaultPaymentGateway implements PaymentGateway {

    @Override
    public boolean initiatePayment(String userId, BigDecimal amount) {
        // TODO: integrate with actual payment provider
        return true;
    }
}

