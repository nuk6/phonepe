package org.example.persistence.inmemory;

import org.example.exception.FundError;
import org.example.exception.PhonePeRuntimeException;
import org.example.model.MutualFund;
import org.example.persistence.MutualFundDao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemoryMutualFundDao implements MutualFundDao {

    private final Map<String, MutualFund> store = new ConcurrentHashMap<>();

    @Override
    public void save(MutualFund fund) {
        store.put(fund.getId(), fund);
    }

    @Override
    public Optional<MutualFund> findById(String fundId) {
        return Optional.ofNullable(store.get(fundId));
    }

    @Override
    public List<MutualFund> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void updateNav(String fundId, BigDecimal nav) {
        MutualFund fund = store.get(fundId);
        if (fund == null) {
            throw new PhonePeRuntimeException(FundError.FUND_NOT_FOUND);
        }
        fund.setCurrentNav(nav);
    }
}

