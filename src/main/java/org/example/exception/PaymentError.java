package org.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentError implements Error {

    PAYMENT_FAILED(502, "PAYMENT_FAILED", "Payment processing failed for the installment");

    private final int status;
    private final String code;
    private final String description;
}

