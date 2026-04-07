package org.example.persistence.postgres;

import jakarta.persistence.LockModeType;
import org.example.enums.SipState;
import org.example.persistence.entity.SipEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Profile({"local", "qa", "prod"})
public interface JpaSipRepository extends JpaRepository<SipEntity, String> {

    List<SipEntity> findByUserId(String userId);

    List<SipEntity> findByState(SipState state);

    @Query("SELECT s FROM SipEntity s WHERE s.state = :state AND s.nextExecutionDate <= :date")
    List<SipEntity> findDueForExecution(@Param("state") SipState state, @Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SipEntity s WHERE s.id = :id")
    Optional<SipEntity> findByIdForUpdate(@Param("id") String id);

    /**
     * Native query — JPA's @Lock doesn't support SKIP LOCKED.
     * Each instance grabs a different batch of rows.
     * Rows locked by another instance are silently skipped.
     */
    @Query(value = "SELECT * FROM sips " +
            "WHERE state = 'ACTIVE' AND next_execution_date <= :date " +
            "ORDER BY next_execution_date " +
            "FOR UPDATE SKIP LOCKED " +
            "LIMIT :batchSize",
            nativeQuery = true)
    List<SipEntity> claimDueSipsForExecution(@Param("date") LocalDate date,
                                            @Param("batchSize") int batchSize);
}

