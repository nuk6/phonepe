package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.MutualFund;
import org.example.model.MutualFundCategory;
import org.example.model.User;
import org.example.persistence.MutualFundDao;
import org.example.persistence.UserDao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MutualFundDao mutualFundDao;
    private final UserDao userDao;

    @Override
    public void run(String... args) {
        List<MutualFund> funds = List.of(
                new MutualFund("MF001", "HDFC Flexi Cap Fund", MutualFundCategory.EQUITY, new BigDecimal("25.50")),
                new MutualFund("MF002", "ICICI Prudential Bluechip Fund", MutualFundCategory.EQUITY, new BigDecimal("72.30")),
                new MutualFund("MF003", "SBI Magnum Gilt Fund", MutualFundCategory.EQUITY, new BigDecimal("48.10")),
                new MutualFund("MF004", "Axis Liquid Fund", MutualFundCategory.DEBT, new BigDecimal("2400.75")),
                new MutualFund("MF005", "Kotak Balanced Advantage Fund", MutualFundCategory.HYBRID, new BigDecimal("15.80"))
        );
        funds.forEach(mutualFundDao::save);

        List<User> users = List.of(
                new User("U001", "Rahul Sharma"),
                new User("U002", "Priya Mehta"),
                new User("U003", "Amit Patel")
        );
        users.forEach(userDao::save);

        log.info("Seeded {} mutual funds and {} users", funds.size(), users.size());
    }
}

