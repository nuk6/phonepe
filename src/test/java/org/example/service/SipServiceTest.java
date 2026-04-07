package org.example.service;

import org.example.model.enums.SipMode;
import org.example.model.enums.SipState;
import org.example.exception.PhonePeRuntimeException;
import org.example.exception.SipError;
import org.example.exception.UserError;
import org.example.exception.FundError;
import org.example.model.MutualFund;
import org.example.model.MutualFundCategory;
import org.example.model.Sip;
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

class SipServiceTest {

    private SipService sipService;
    private InMemorySipDao sipDao;
    private InMemoryMutualFundDao fundDao;
    private InMemoryUserDao userDao;
    private InMemorySipInstallmentDao installmentDao;

    @BeforeEach
    void setUp() {
        sipDao = new InMemorySipDao();
        fundDao = new InMemoryMutualFundDao();
        userDao = new InMemoryUserDao();
        installmentDao = new InMemorySipInstallmentDao();
        PaymentGateway pg = (userId, amount, key) -> true; // always succeeds

        sipService = new SipService(sipDao, installmentDao, fundDao, userDao, pg);

        userDao.save(new User("u1", "Rahul"));
        fundDao.save(new MutualFund("f1", "HDFC Flexi Cap", MutualFundCategory.EQUITY, new BigDecimal("50.00")));
    }

    // --- create ---

    @Test
    void createSip_happyPath() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("2000"),
                SipMode.MONTHLY, LocalDate.of(2026, 4, 10), 0);

        assertEquals(SipState.ACTIVE, sip.getState());
        assertEquals(0, sip.getInstallmentCount());
        assertEquals(LocalDate.of(2026, 4, 10), sip.getNextExecutionDate());
    }

    @Test
    void createSip_invalidUser_throws() {
        PhonePeRuntimeException ex = assertThrows(PhonePeRuntimeException.class,
                () -> sipService.createSip("ghost", "f1", new BigDecimal("1000"),
                        SipMode.MONTHLY, LocalDate.now(), 0));
        assertEquals(UserError.USER_NOT_FOUND, ex.getError());
    }

    @Test
    void createSip_invalidFund_throws() {
        PhonePeRuntimeException ex = assertThrows(PhonePeRuntimeException.class,
                () -> sipService.createSip("u1", "nope", new BigDecimal("1000"),
                        SipMode.WEEKLY, LocalDate.now(), 0));
        assertEquals(FundError.FUND_NOT_FOUND, ex.getError());
    }

    @Test
    void createSip_zeroAmount_throws() {
        PhonePeRuntimeException ex = assertThrows(PhonePeRuntimeException.class,
                () -> sipService.createSip("u1", "f1", BigDecimal.ZERO,
                        SipMode.MONTHLY, LocalDate.now(), 0));
        assertEquals(SipError.INVALID_AMOUNT, ex.getError());
    }

    @Test
    void createSip_negativeStepUp_throws() {
        PhonePeRuntimeException ex = assertThrows(PhonePeRuntimeException.class,
                () -> sipService.createSip("u1", "f1", new BigDecimal("500"),
                        SipMode.MONTHLY, LocalDate.now(), -5));
        assertEquals(SipError.INVALID_STEP_UP_PERCENTAGE, ex.getError());
    }

    // --- pause / unpause / stop ---

    @Test
    void pauseSip_changesStateCorrectly() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, LocalDate.now(), 0);

        Sip paused = sipService.pauseSip(sip.getId());
        assertEquals(SipState.PAUSED, paused.getState());
    }

    @Test
    void pauseSip_alreadyPaused_throws() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, LocalDate.now(), 0);
        sipService.pauseSip(sip.getId());

        PhonePeRuntimeException ex = assertThrows(PhonePeRuntimeException.class,
                () -> sipService.pauseSip(sip.getId()));
        assertEquals(SipError.SIP_ALREADY_PAUSED, ex.getError());
    }

    @Test
    void unpauseSip_bringsBackToActive() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.WEEKLY, LocalDate.now(), 0);
        sipService.pauseSip(sip.getId());

        Sip unpaused = sipService.unpauseSip(sip.getId());
        assertEquals(SipState.ACTIVE, unpaused.getState());
    }

    @Test
    void unpauseSip_alreadyActive_throws() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.WEEKLY, LocalDate.now(), 0);

        PhonePeRuntimeException ex = assertThrows(PhonePeRuntimeException.class,
                () -> sipService.unpauseSip(sip.getId()));
        assertEquals(SipError.SIP_ALREADY_ACTIVE, ex.getError());
    }

    @Test
    void stopSip_cannotPauseAfterStop() {
        Sip sip = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, LocalDate.now(), 0);
        sipService.stopSip(sip.getId());

        PhonePeRuntimeException ex = assertThrows(PhonePeRuntimeException.class,
                () -> sipService.pauseSip(sip.getId()));
        assertEquals(SipError.SIP_STOPPED, ex.getError());
    }

    // --- portfolio ---

    @Test
    void getUserPortfolio_showsAllStates() {
        Sip s1 = sipService.createSip("u1", "f1", new BigDecimal("1000"),
                SipMode.MONTHLY, LocalDate.now(), 0);
        Sip s2 = sipService.createSip("u1", "f1", new BigDecimal("2000"),
                SipMode.WEEKLY, LocalDate.now(), 0);
        sipService.pauseSip(s1.getId());
        sipService.stopSip(s2.getId());

        List<Sip> portfolio = sipService.getUserPortfolio("u1");
        assertEquals(2, portfolio.size());
    }
}

