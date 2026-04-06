package org.example.persistence;

import org.example.model.MutualFund;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MutualFundDao {

    void save(MutualFund fund);

    Optional<MutualFund> findById(String fundId);

    List<MutualFund> findAll();

    void updateNav(String fundId, BigDecimal nav);
}

