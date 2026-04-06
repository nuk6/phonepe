package org.example.persistence.postgres;

import org.example.enums.SipState;
import org.example.persistence.entity.SipEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Profile({"qa", "prod"})
public interface JpaSipRepository extends JpaRepository<SipEntity, String> {

    List<SipEntity> findByUserId(String userId);

    List<SipEntity> findByState(SipState state);

    @Query("SELECT s FROM SipEntity s WHERE s.state = 'ACTIVE' AND s.nextExecutionDate <= :date")
    List<SipEntity> findDueForExecution(@Param("date") LocalDate date);
}

