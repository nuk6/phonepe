package org.example.persistence.postgres;

import lombok.RequiredArgsConstructor;
import org.example.exception.FundError;
import org.example.exception.PhonePeRuntimeException;
import org.example.model.MutualFund;
import org.example.persistence.MutualFundDao;
import org.example.persistence.entity.MutualFundEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"local", "qa", "prod"})
@RequiredArgsConstructor
public class PostgresMutualFundDao implements MutualFundDao {

    private final JpaMutualFundRepository jpaRepo;

    @Override
    public void save(MutualFund fund) {
        jpaRepo.save(MutualFundEntity.fromDomain(fund));
    }

    @Override
    public Optional<MutualFund> findById(String fundId) {
        return jpaRepo.findById(fundId).map(MutualFundEntity::toDomain);
    }

    @Override
    public List<MutualFund> findAll() {
        return jpaRepo.findAll().stream()
                .map(MutualFundEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void updateNav(String fundId, BigDecimal nav) {
        int updated = jpaRepo.updateNavById(fundId, nav);
        if (updated == 0) {
            throw new PhonePeRuntimeException(FundError.FUND_NOT_FOUND);
        }
    }
}

