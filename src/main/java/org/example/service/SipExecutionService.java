package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Sip;
import org.example.model.SipInstallment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator — deliberately NOT @Transactional.
 *
 * Each SIP execution follows a 3-phase protocol that safely coordinates
 * the external payment call with local DB state:
 *
 *   Phase 1 (own txn): save installment as PENDING
 *   Phase 2 (no txn):  call payment gateway
 *   Phase 3 (own txn): finalize as SUCCESS or FAILED
 *
 * If the app crashes between phases, a reconciliation job can find
 * PENDING installments and query the payment gateway by idempotency key
 * to learn the true outcome.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SipExecutionService {

    private static final int BATCH_SIZE = 50;

    private final SipExecutionHelper helper;
    private final PaymentGateway paymentGateway;

    /**
     * Claims a batch of due SIPs using SELECT ... FOR UPDATE SKIP LOCKED.
     * Each app instance gets a different non-overlapping batch — no two
     * instances process the same SIP.
     *
     * Single batch per call. If there are more due SIPs than BATCH_SIZE,
     * the next scheduler tick (or another instance) picks them up.
     */
    public List<SipInstallment> executeAllDueSips(LocalDate today) {
        List<Sip> batch = helper.claimBatch(today, BATCH_SIZE);
        List<SipInstallment> results = new ArrayList<>();

        for (Sip sip : batch) {
            try {
                SipInstallment installment = executeSingleSip(sip, today);
                results.add(installment);
            } catch (Exception e) {
                log.error("Failed to execute SIP {}: {}", sip.getId(), e.getMessage());
            }
        }

        return results;
    }

    private SipInstallment executeSingleSip(Sip sip, LocalDate executionDate) {
        // Phase 1: persist PENDING installment (committed independently)
        SipInstallment pending = helper.savePendingInstallment(sip, executionDate);

        // Phase 2: call payment gateway (NO transaction — external side effect)
        boolean paid = paymentGateway.initiatePayment(
                sip.getUserId(), sip.getAmount(), pending.getIdempotencyKey());

        // Phase 3: finalize based on payment outcome (committed independently)
        if (!paid) {
            helper.finalizeFailure(pending);
            log.error("Payment failed for SIP {} installment {}", sip.getId(), pending.getId());
            return pending;
        }

        return helper.finalizeSuccess(pending, sip, executionDate);
    }
}

