package org.example.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Order of execution (outermost → innermost):
 *
 *   Bulkhead → CircuitBreaker → Retry → actual call
 *
 * 1. Bulkhead checks: "are there too many concurrent calls already?"
 *    - YES → rejects immediately, goes to fallback
 *    - NO  → acquires a permit, proceeds ↓
 *
 * 2. CircuitBreaker checks: "is the circuit open?"
 *    - OPEN → skips the call, goes to fallback
 *    - CLOSED/HALF_OPEN → proceeds ↓
 *
 * 3. Retry wraps the actual call:
 *    - Call fails → waits 500ms → retries (up to 3 attempts)
 *    - All retries exhausted → exception bubbles up to circuit breaker
 *      (which records it as a failure for its sliding window)
 *
 * 4. If everything fails → fallback returns false → installment marked FAILED
 */
@Slf4j
@Component
public class DefaultPaymentGateway implements PaymentGateway {

    @Override
    @Bulkhead(name = "paymentGateway", fallbackMethod = "paymentFallback")
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentGateway")
    public boolean initiatePayment(String userId, BigDecimal amount, String idempotencyKey) {
        // stub — real impl would call external payment service via HTTP
        // idempotencyKey prevents double-charge on retries
        log.debug("Initiating payment: user={}, amount={}, key={}", userId, amount, idempotencyKey);
        return true;
    }

    /**
     * Fallback — invoked when:
     * - Bulkhead rejects (too many concurrent calls)
     * - Circuit is open (too many recent failures)
     * - All retries exhausted
     *
     * Must have same signature + Throwable at the end.
     */
    private boolean paymentFallback(String userId, BigDecimal amount,
                                    String idempotencyKey, Throwable t) {
        log.error("Payment failed for user={}, key={}, reason={}: {}",
                userId, idempotencyKey, t.getClass().getSimpleName(), t.getMessage());
        return false;
    }
}

