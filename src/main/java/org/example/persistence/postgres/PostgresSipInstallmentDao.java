package org.example.persistence.postgres;

import lombok.RequiredArgsConstructor;
import org.example.model.SipInstallment;
import org.example.persistence.SipInstallmentDao;
import org.example.persistence.entity.SipInstallmentEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"local", "qa", "prod"})
@RequiredArgsConstructor
public class PostgresSipInstallmentDao implements SipInstallmentDao {

    private final JpaSipInstallmentRepository jpaRepo;

    @Override
    public void save(SipInstallment installment) {
        jpaRepo.save(SipInstallmentEntity.fromDomain(installment));
    }

    @Override
    public List<SipInstallment> findBySipId(String sipId) {
        return jpaRepo.findBySipId(sipId).stream()
                .map(SipInstallmentEntity::toDomain)
                .toList();
    }
}

