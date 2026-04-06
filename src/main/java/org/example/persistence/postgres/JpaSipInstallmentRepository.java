package org.example.persistence.postgres;

import org.example.persistence.entity.SipInstallmentEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Profile({"qa", "prod"})
public interface JpaSipInstallmentRepository extends JpaRepository<SipInstallmentEntity, String> {

    List<SipInstallmentEntity> findBySipId(String sipId);
}

