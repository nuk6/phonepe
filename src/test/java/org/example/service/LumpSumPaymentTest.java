package org.example.service;

import org.example.model.enums.SipMode;
import org.example.model.enums.SipState;
import org.example.exception.PhonePeRuntimeException;
import org.example.exception.SipError;
import org.example.model.MutualFund;
import org.example.model.MutualFundCategory;
import org.example.model.Sip;
import org.example.model.SipInstallment;
import org.example.model.User;
import org.example.persistence.inmemory.InMemoryMutualFundDao;
import org.example.persistence.inmemory.InMemorySipDao;
import org.example.persistence.inmemory.InMemorySipInstallmentDao;
import org.example.persistence.inmemory.InMemoryUserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LumpSumPaymentTest {

    private SipService sipService;
    private InMemorySipDao sipDao;
    private InMemoryMutualFundDao fundDao;

    @BeforeEach
    void setUp() {
        sipDao = new InMemorySipDao();
        fundDao = new InMemoryMutualFundDao();
        InMemoryUserDao userDao = new InMemoryUserDao();
        InMemorySipInstallmentDao installmentDao = new InMemorySipInstallmentDao();

        sipService = new SipService(sipDao, installmentDao, fundDao, userDao,
                (userId, amount, key) -> true);

        userDao.save(new User("u1", "Priya"));
        fundDao.save(new MutualFund("f1", "ICICI Blue", MutualFundCategory.EQUITY, new BigDecimal("50.00")));
    }

    @Test
    void lumpSum_reactivatesPausedSip() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, LocalDate.of(2026, 1, 1), 0);
        sipService.pauseSip(sip.getId());

        List<SipInstallment> installments = sipService.lumpSumPayment(sip.getId(), 3);

        assertEquals(3, installments.size());

        Sip updated = sipDao.findById(sip.getId()).orElseThrow();
        assertEquals(SipState.ACTIVE, updated.getState());
        assertEquals(3, updated.getInstallmentCount());
    }

    @Test
    void lumpSum_withStepUp_eachInstallmentIncreases() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, LocalDate.of(2026, 1, 1), 10);
        sipService.pauseSip(sip.getId());

        List<SipInstallment> installments = sipService.lumpSumPayment(sip.getId(), 3);

        // 1000 -> 1100 -> 1210
        assertEquals(0, new BigDecimal("1000").compareTo(installments.get(0).getAmount()));
        assertEquals(0, new BigDecimal("1100.00").compareTo(installments.get(1).getAmount()));
        assertEquals(0, new BigDecimal("1210.00").compareTo(installments.get(2).getAmount()));

        // after 3 missed, next installment amount should be 1331
        Sip updated = sipDao.findById(sip.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("1331.00").compareTo(updated.getAmount()));
    }

    @Test
    void lumpSum_withoutStepUp_allInstallmentsSameAmount() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("2000"),
                SipMode.WEEKLY, LocalDate.of(2026, 3, 1), 0);
        sipService.pauseSip(sip.getId());

        List<SipInstallment> installments = sipService.lumpSumPayment(sip.getId(), 4);

        for (SipInstallment inst : installments) {
            assertEquals(0, new BigDecimal("2000").compareTo(inst.getAmount()));
        }
    }

    @Test
    void lumpSum_onStoppedSip_throws() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, LocalDate.of(2026, 1, 1), 0);
        sipService.stopSip(sip.getId());

        PhonePeRuntimeException ex = assertThrows(PhonePeRuntimeException.class,
                () -> sipService.lumpSumPayment(sip.getId(), 2));
        assertEquals(SipError.SIP_STOPPED, ex.getError());
    }

    @Test
    void lumpSum_unitsCalculatedAtCurrentNav() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, LocalDate.of(2026, 1, 1), 0);
        sipService.pauseSip(sip.getId());

        // change NAV before lump sum
        fundDao.updateNav("f1", new BigDecimal("200.00"));

        List<SipInstallment> installments = sipService.lumpSumPayment(sip.getId(), 2);
        // 1000 / 200 = 5
        for (SipInstallment inst : installments) {
            assertEquals(new BigDecimal("5.0000"), inst.getUnitsAllotted());
        }
    }
}

