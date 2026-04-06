package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.enums.InstallmentStatus;
import org.example.enums.SipMode;
import org.example.enums.SipState;
import org.example.exception.FundError;
import org.example.exception.PaymentError;
import org.example.exception.PhonePeRuntimeException;
import org.example.exception.SipError;
import org.example.exception.UserError;
import org.example.model.MutualFund;
import org.example.model.Sip;
import org.example.model.SipInstallment;
import org.example.persistence.MutualFundDao;
import org.example.persistence.SipDao;
import org.example.persistence.SipInstallmentDao;
import org.example.persistence.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SipService {

    private final SipDao sipDao;
    private final SipInstallmentDao installmentDao;
    private final MutualFundDao mutualFundDao;
    private final UserDao userDao;
    private final PaymentGateway paymentGateway;

    public Sip createSip(String userId, String fundId, BigDecimal amount,
                         SipMode mode, LocalDate startDate, double stepUpPercentage) {
        userDao.findById(userId)
                .orElseThrow(() -> new PhonePeRuntimeException(UserError.USER_NOT_FOUND));
        mutualFundDao.findById(fundId)
                .orElseThrow(() -> new PhonePeRuntimeException(FundError.FUND_NOT_FOUND));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PhonePeRuntimeException(SipError.INVALID_AMOUNT);
        }
        if (stepUpPercentage < 0 || stepUpPercentage > 100) {
            throw new PhonePeRuntimeException(SipError.INVALID_STEP_UP_PERCENTAGE);
        }

        String sipId = UUID.randomUUID().toString();
        Sip sip = new Sip(sipId, userId, fundId, amount, mode, startDate, stepUpPercentage);
        sipDao.save(sip);
        return sip;
    }

    public Sip pauseSip(String sipId) {
        Sip sip = findActiveSip(sipId);
        if (sip.getState() == SipState.PAUSED) {
            throw new PhonePeRuntimeException(SipError.SIP_ALREADY_PAUSED);
        }
        sip.setState(SipState.PAUSED);
        sipDao.update(sip);
        return sip;
    }

    public Sip unpauseSip(String sipId) {
        Sip sip = getSipOrThrow(sipId);
        if (sip.getState() == SipState.STOPPED) {
            throw new PhonePeRuntimeException(SipError.SIP_STOPPED);
        }
        if (sip.getState() == SipState.ACTIVE) {
            throw new PhonePeRuntimeException(SipError.SIP_ALREADY_ACTIVE);
        }
        sip.setState(SipState.ACTIVE);
        sipDao.update(sip);
        return sip;
    }

    public Sip stopSip(String sipId) {
        Sip sip = getSipOrThrow(sipId);
        if (sip.getState() == SipState.STOPPED) {
            throw new PhonePeRuntimeException(SipError.SIP_STOPPED);
        }
        sip.setState(SipState.STOPPED);
        sipDao.update(sip);
        return sip;
    }

    public List<Sip> getUserPortfolio(String userId) {
        userDao.findById(userId)
                .orElseThrow(() -> new PhonePeRuntimeException(UserError.USER_NOT_FOUND));
        return sipDao.findByUserId(userId);
    }

    /**
     * Lump sum payment covers missed installments for a paused SIP.
     * After payment, the SIP is reactivated with next execution date set from today.
     */
    @Transactional
    public List<SipInstallment> lumpSumPayment(String sipId, int missedInstallments) {
        Sip sip = getSipOrThrow(sipId);
        if (sip.getState() == SipState.STOPPED) {
            throw new PhonePeRuntimeException(SipError.SIP_STOPPED);
        }

        MutualFund fund = mutualFundDao.findById(sip.getFundId())
                .orElseThrow(() -> new PhonePeRuntimeException(FundError.FUND_NOT_FOUND));

        // pre-compute each installment amount (respecting step-up)
        BigDecimal currentAmount = sip.getAmount();
        double stepUp = sip.getStepUpPercentage();
        BigDecimal multiplier = BigDecimal.valueOf(1 + stepUp / 100.0);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal[] amounts = new BigDecimal[missedInstallments];
        for (int i = 0; i < missedInstallments; i++) {
            amounts[i] = currentAmount;
            totalAmount = totalAmount.add(currentAmount);
            if (stepUp > 0) {
                currentAmount = currentAmount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            }
        }

        String lumpSumKey = sipId + "_lumpsum_" + System.currentTimeMillis();
        boolean paid = paymentGateway.initiatePayment(sip.getUserId(), totalAmount, lumpSumKey);
        if (!paid) {
            throw new PhonePeRuntimeException(PaymentError.PAYMENT_FAILED);
        }

        BigDecimal nav = fund.getCurrentNav();
        List<SipInstallment> installments = new ArrayList<>();

        for (int i = 0; i < missedInstallments; i++) {
            String idempotencyKey = sipId + "_lumpsum_" + sip.getInstallmentCount();
            BigDecimal units = amounts[i].divide(nav, 4, RoundingMode.HALF_UP);
            SipInstallment inst = new SipInstallment(
                    UUID.randomUUID().toString(), sipId, amounts[i],
                    nav, units, LocalDate.now(), InstallmentStatus.SUCCESS, idempotencyKey
            );
            installmentDao.save(inst);
            installments.add(inst);
            sip.incrementInstallmentCount();
        }

        // update SIP amount to the stepped-up value after all missed installments
        sip.setAmount(currentAmount);
        sip.setState(SipState.ACTIVE);
        sip.setNextExecutionDate(computeNextDate(LocalDate.now(), sip.getMode()));
        sipDao.update(sip);

        return installments;
    }

    // -- helpers --

    private Sip getSipOrThrow(String sipId) {
        return sipDao.findById(sipId)
                .orElseThrow(() -> new PhonePeRuntimeException(SipError.SIP_NOT_FOUND));
    }

    private Sip findActiveSip(String sipId) {
        Sip sip = getSipOrThrow(sipId);
        if (sip.getState() == SipState.STOPPED) {
            throw new PhonePeRuntimeException(SipError.SIP_STOPPED);
        }
        return sip;
    }

    static LocalDate computeNextDate(LocalDate from, SipMode mode) {
        return switch (mode) {
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case QUARTERLY -> from.plusMonths(3);
        };
    }
}

