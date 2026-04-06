package org.example.service;

import org.example.enums.InstallmentStatus;
import org.example.enums.SipMode;
import org.example.enums.SipState;
import org.example.model.MutualFund;
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

class SipExecutionServiceTest {

    private SipService sipService;
    private SipExecutionService executionService;
    private InMemorySipDao sipDao;
    private InMemoryMutualFundDao fundDao;
    private InMemorySipInstallmentDao installmentDao;
    private boolean paymentShouldSucceed;

    @BeforeEach
    void setUp() {
        sipDao = new InMemorySipDao();
        fundDao = new InMemoryMutualFundDao();
        installmentDao = new InMemorySipInstallmentDao();
        InMemoryUserDao userDao = new InMemoryUserDao();
        paymentShouldSucceed = true;
        PaymentGateway pg = (userId, amount) -> paymentShouldSucceed;

        sipService = new SipService(sipDao, installmentDao, fundDao, userDao, pg);
        executionService = new SipExecutionService(sipDao, installmentDao, fundDao, pg);

        userDao.save(new User("u1", "Amit"));
        fundDao.save(new MutualFund("f1", "SBI Bluechip", "Equity", new BigDecimal("100.00")));
    }

    @Test
    void executeOnDueDate_createsInstallmentAndAdvancesDate() {
        LocalDate start = LocalDate.of(2026, 4, 10);
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("5000"),
                SipMode.MONTHLY, start, 0);

        List<SipInstallment> result = executionService.executeAllDueSips(start);

        assertEquals(1, result.size());
        SipInstallment inst = result.get(0);
        assertEquals(InstallmentStatus.SUCCESS, inst.getStatus());
        // 5000 / 100 = 50 units
        assertEquals(new BigDecimal("50.0000"), inst.getUnitsAllotted());

        // next execution should be May 10
        Sip updated = sipDao.findById(sip.getId()).orElseThrow();
        assertEquals(LocalDate.of(2026, 5, 10), updated.getNextExecutionDate());
        assertEquals(1, updated.getInstallmentCount());
    }

    @Test
    void executeBeforeDueDate_doesNothing() {
        LocalDate start = LocalDate.of(2026, 4, 15);
        sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.WEEKLY, start, 0);

        // try executing a day before
        List<SipInstallment> result = executionService.executeAllDueSips(start.minusDays(1));
        assertTrue(result.isEmpty());
    }

    @Test
    void pausedSipIsSkippedDuringExecution() {
        LocalDate start = LocalDate.of(2026, 4, 6);
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.WEEKLY, start, 0);
        sipService.pauseSip(sip.getId());

        List<SipInstallment> result = executionService.executeAllDueSips(start);
        assertTrue(result.isEmpty(), "paused SIP should not get executed");
    }

    @Test
    void stepUp_increasesAmountAfterEachExecution() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, start, 10);

        // month 1
        executionService.executeAllDueSips(start);
        Sip after1 = sipDao.findById(sip.getId()).orElseThrow();
        assertEquals(new BigDecimal("1100.00"), after1.getAmount());

        // month 2
        executionService.executeAllDueSips(LocalDate.of(2026, 2, 1));
        Sip after2 = sipDao.findById(sip.getId()).orElseThrow();
        assertEquals(new BigDecimal("1210.00"), after2.getAmount());

        // month 3
        executionService.executeAllDueSips(LocalDate.of(2026, 3, 1));
        Sip after3 = sipDao.findById(sip.getId()).orElseThrow();
        assertEquals(new BigDecimal("1331.00"), after3.getAmount());
        assertEquals(3, after3.getInstallmentCount());
    }

    @Test
    void executionUsesLatestNav_notCreationNav() {
        LocalDate start = LocalDate.of(2026, 4, 6);
        sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, start, 0);

        // change the NAV before execution
        fundDao.updateNav("f1", new BigDecimal("200.00"));

        List<SipInstallment> result = executionService.executeAllDueSips(start);
        assertEquals(1, result.size());
        // 1000 / 200 = 5
        assertEquals(new BigDecimal("5.0000"), result.get(0).getUnitsAllotted());
        assertEquals(new BigDecimal("200.00"), result.get(0).getNav());
    }

    @Test
    void paymentFailure_recordsFailedInstallment_andDoesNotAdvanceSip() {
        LocalDate start = LocalDate.of(2026, 4, 6);
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, start, 0);

        paymentShouldSucceed = false;

        List<SipInstallment> result = executionService.executeAllDueSips(start);
        // execution swallows the exception, returns empty
        assertTrue(result.isEmpty());

        // but a FAILED installment should still be recorded
        List<SipInstallment> saved = installmentDao.findBySipId(sip.getId());
        assertEquals(1, saved.size());
        assertEquals(InstallmentStatus.FAILED, saved.get(0).getStatus());

        // sip should NOT have advanced
        Sip unchanged = sipDao.findById(sip.getId()).orElseThrow();
        assertEquals(0, unchanged.getInstallmentCount());
        assertEquals(start, unchanged.getNextExecutionDate());
    }

    @Test
    void weeklySip_preservesDayOfWeek() {
        // Apr 7 2026 is a Tuesday
        LocalDate tuesday = LocalDate.of(2026, 4, 7);
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("500"),
                SipMode.WEEKLY, tuesday, 0);

        executionService.executeAllDueSips(tuesday);

        Sip updated = sipDao.findById(sip.getId()).orElseThrow();
        // next should also be a Tuesday
        assertEquals(LocalDate.of(2026, 4, 14), updated.getNextExecutionDate());
        assertEquals(tuesday.getDayOfWeek(), updated.getNextExecutionDate().getDayOfWeek());
    }
}

