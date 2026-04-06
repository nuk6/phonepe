package org.example.persistence.postgres;

import lombok.RequiredArgsConstructor;
import org.example.enums.SipState;
import org.example.model.Sip;
import org.example.persistence.SipDao;
import org.example.persistence.entity.SipEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"local", "qa", "prod"})
@RequiredArgsConstructor
public class PostgresSipDao implements SipDao {

    private final JpaSipRepository jpaRepo;

    @Override
    public void save(Sip sip) {
        jpaRepo.save(SipEntity.fromDomain(sip));
    }

    @Override
    public Optional<Sip> findById(String sipId) {
        return jpaRepo.findById(sipId).map(SipEntity::toDomain);
    }

    @Override
    public List<Sip> findByUserId(String userId) {
        return jpaRepo.findByUserId(userId).stream()
                .map(SipEntity::toDomain)
                .toList();
    }

    @Override
    public List<Sip> findByState(SipState state) {
        return jpaRepo.findByState(state).stream()
                .map(SipEntity::toDomain)
                .toList();
    }

    @Override
    public List<Sip> findDueForExecution(LocalDate date) {
        return jpaRepo.findDueForExecution(date).stream()
                .map(SipEntity::toDomain)
                .toList();
    }

    @Override
    public void update(Sip sip) {
        jpaRepo.save(SipEntity.fromDomain(sip));
    }
}

