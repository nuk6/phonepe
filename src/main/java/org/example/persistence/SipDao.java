package org.example.persistence;

import org.example.enums.SipState;
import org.example.model.Sip;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SipDao {

    void save(Sip sip);

    Optional<Sip> findById(String sipId);

    /** Acquires a row-level lock in DB implementations. Falls back to findById for in-memory. */
    default Optional<Sip> findByIdForUpdate(String sipId) {
        return findById(sipId);
    }

    List<Sip> findByUserId(String userId);

    List<Sip> findByState(SipState state);

    List<Sip> findDueForExecution(LocalDate date);

    void update(Sip sip);
}

