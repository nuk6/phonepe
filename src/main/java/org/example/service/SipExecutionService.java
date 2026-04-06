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

    private final SipDao sipDao;
    private final SipInstallmentDao installmentDao;
    private final MutualFundDao mutualFundDao;
    private final PaymentGateway paymentGateway;

    public List<SipInstallment> executeAllDueSips(LocalDate today) {
        List<Sip> dueSips = sipDao.findDueForExecution(today);
        List<SipInstallment> results = new ArrayList<>();

        for (Sip sip : dueSips) {
            try {
                SipInstallment installment = executeSingleSip(sip.getId(), today);
                results.add(installment);
            } catch (PhonePeRuntimeException e) {
                log.error("Failed to execute SIP {}: {}", sip.getId(), e.getError().getDescription());
            }
        }

        return results;
    }

    @Transactional
    public SipInstallment executeSingleSip(String sipId, LocalDate executionDate) {
        // re-fetch inside transaction for locking (Postgres DAO uses FOR UPDATE)
        Sip sip = sipDao.findByIdForUpdate(sipId)
                .orElseGet(() -> sipDao.findById(sipId)
                        .orElseThrow(() -> new PhonePeRuntimeException(
                                org.example.exception.SipError.SIP_NOT_FOUND)));

        MutualFund fund = mutualFundDao.findById(sip.getFundId())
                .orElseThrow(() -> new PhonePeRuntimeException(FundError.FUND_NOT_FOUND));

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

