package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.InstallmentStatus;
import org.example.exception.FundError;
import org.example.exception.PaymentError;
import org.example.exception.PhonePeRuntimeException;
import org.example.model.MutualFund;
import org.example.model.Sip;
import org.example.model.SipInstallment;
import org.example.persistence.MutualFundDao;
import org.example.persistence.SipDao;
import org.example.persistence.SipInstallmentDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SipExecutionService {

    private static final int BATCH_SIZE = 50;

    private final SipDao sipDao;
    private final SipInstallmentDao installmentDao;
    private final MutualFundDao mutualFundDao;
    private final PaymentGateway paymentGateway;

    /**
     * Claims a batch of due SIPs using SELECT ... FOR UPDATE SKIP LOCKED.
     * Each app instance gets a different non-overlapping batch — no two
     * instances process the same SIP.
     *
     * Single batch per call. If there are more due SIPs than BATCH_SIZE,
     * the next scheduler tick (or another instance) picks them up.
     */
    @Transactional
    public List<SipInstallment> executeAllDueSips(LocalDate today) {
        List<Sip> batch = sipDao.claimDueSipsForExecution(today, BATCH_SIZE);
        List<SipInstallment> results = new ArrayList<>();

        for (Sip sip : batch) {
            try {
                SipInstallment installment = executeSingleSip(sip, today);
                results.add(installment);
            } catch (PhonePeRuntimeException e) {
                log.error("Failed to execute SIP {}: {}", sip.getId(), e.getError().getDescription());
            }
        }

        return results;
    }

    private SipInstallment executeSingleSip(Sip sip, LocalDate executionDate) {
        // rows are already locked by claimDueSipsForExecution — no need for findByIdForUpdate

        MutualFund fund = mutualFundDao.findById(sip.getFundId())
                .orElseThrow(() -> new PhonePeRuntimeException(FundError.FUND_NOT_FOUND));

        String sipId = sip.getId();
        BigDecimal amount = sip.getAmount();
        BigDecimal nav = fund.getCurrentNav();

        // idempotency key = sipId + installmentCount — deterministic, prevents double-charge
        String idempotencyKey = sipId + "_" + sip.getInstallmentCount();

        boolean paid = paymentGateway.initiatePayment(sip.getUserId(), amount, idempotencyKey);
        if (!paid) {
            SipInstallment failed = buildInstallment(sipId, amount, nav,
                    BigDecimal.ZERO, executionDate, InstallmentStatus.FAILED, idempotencyKey);
            installmentDao.save(failed);
            throw new PhonePeRuntimeException(PaymentError.PAYMENT_FAILED);
        }

        BigDecimal units = amount.divide(nav, 4, RoundingMode.HALF_UP);
        SipInstallment installment = buildInstallment(sipId, amount, nav,
                units, executionDate, InstallmentStatus.SUCCESS, idempotencyKey);
        installmentDao.save(installment);

        sip.incrementInstallmentCount();
        applyStepUp(sip);
        sip.setNextExecutionDate(SipService.computeNextDate(executionDate, sip.getMode()));
        sipDao.update(sip);

        log.info("Executed SIP {} | amount={} | nav={} | units={}", sipId, amount, nav, units);
        return installment;
    }

    private void applyStepUp(Sip sip) {
        double pct = sip.getStepUpPercentage();
        if (pct <= 0) {
            return;
        }
        BigDecimal multiplier = BigDecimal.valueOf(1 + pct / 100.0);
        BigDecimal newAmount = sip.getAmount().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        sip.setAmount(newAmount);
    }

    private SipInstallment buildInstallment(String sipId, BigDecimal amount, BigDecimal nav,
                                            BigDecimal units, LocalDate date,
                                            InstallmentStatus status, String idempotencyKey) {
        return new SipInstallment(UUID.randomUUID().toString(), sipId, amount,
                nav, units, date, status, idempotencyKey);
    }
}

