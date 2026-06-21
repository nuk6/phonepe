package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.FundError;
import org.example.exception.PhonePeRuntimeException;
import org.example.model.MutualFund;
import org.example.model.Sip;
import org.example.model.SipInstallment;
import org.example.model.enums.InstallmentStatus;
import org.example.persistence.MutualFundDao;
import org.example.persistence.SipDao;
import org.example.persistence.SipInstallmentDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Holds the transactional phases for SIP execution.
 * Extracted into a separate bean so that Spring's proxy-based @Transactional
 * works correctly (avoids self-invocation pitfall).
 *
 * Each method runs in its own transaction (REQUIRES_NEW) so that:
 * - The PENDING record is committed before the payment call
 * - A failure in finalization doesn't roll back the PENDING record
 * - One SIP's failure doesn't affect another SIP's committed data
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SipExecutionHelper {

    private final SipDao sipDao;
    private final SipInstallmentDao installmentDao;
    private final MutualFundDao mutualFundDao;

    /**
     * Phase 0: Claim a batch of due SIPs with row-level locks.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Sip> claimBatch(LocalDate today, int batchSize) {
        return sipDao.claimDueSipsForExecution(today, batchSize);
    }

    /**
     * Phase 1: Save a PENDING installment before calling the payment gateway.
     * This is committed independently so that a crash after payment can be
     * recovered by a reconciliation job that finds PENDING installments.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SipInstallment savePendingInstallment(Sip sip, LocalDate executionDate) {
        MutualFund fund = mutualFundDao.findById(sip.getFundId())
                .orElseThrow(() -> new PhonePeRuntimeException(FundError.FUND_NOT_FOUND));

        String idempotencyKey = sip.getId() + "_" + sip.getInstallmentCount();

        SipInstallment pending = new SipInstallment(
                UUID.randomUUID().toString(),
                sip.getId(),
                sip.getAmount(),
                fund.getCurrentNav(),
                BigDecimal.ZERO,           // units not yet known
                executionDate,
                InstallmentStatus.PENDING,
                idempotencyKey
        );
        installmentDao.save(pending);
        return pending;
    }

    /**
     * Phase 3a: Payment succeeded — mark installment SUCCESS, update SIP state.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SipInstallment finalizeSuccess(SipInstallment installment, Sip sip, LocalDate executionDate) {
        BigDecimal nav = installment.getNav();
        BigDecimal units = installment.getAmount()
                .divide(nav, 4, java.math.RoundingMode.HALF_UP);

        installment.setStatus(InstallmentStatus.SUCCESS);
        installment.setUnitsAllotted(units);
        installmentDao.update(installment);

        sip.incrementInstallmentCount();
        applyStepUp(sip);
        sip.setNextExecutionDate(SipService.computeNextDate(executionDate, sip.getMode()));
        sipDao.update(sip);

        log.info("Executed SIP {} | amount={} | nav={} | units={}",
                sip.getId(), installment.getAmount(), nav, units);
        return installment;
    }

    /**
     * Phase 3b: Payment failed — mark installment FAILED. SIP state is NOT advanced.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeFailure(SipInstallment installment) {
        installment.setStatus(InstallmentStatus.FAILED);
        installmentDao.update(installment);
        log.warn("Payment failed for installment {} (SIP {})",
                installment.getId(), installment.getSipId());
    }

    private void applyStepUp(Sip sip) {
        double pct = sip.getStepUpPercentage();
        if (pct <= 0) {
            return;
        }
        BigDecimal multiplier = BigDecimal.valueOf(1 + pct / 100.0);
        BigDecimal newAmount = sip.getAmount()
                .multiply(multiplier)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        sip.setAmount(newAmount);
    }
}

