package org.example.service;

import java.math.BigDecimal;

public interface PaymentGateway {

    boolean initiatePayment(String userId, BigDecimal amount, String idempotencyKey);
}

