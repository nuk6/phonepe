package org.example.persistence.inmemory;

import org.example.model.enums.SipState;
import org.example.model.Sip;
import org.example.persistence.SipDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemorySipDao implements SipDao {

    private final Map<String, Sip> store = new ConcurrentHashMap<>();

    @Override
    public void save(Sip sip) {
        store.put(sip.getId(), sip);
    }

    @Override
    public Optional<Sip> findById(String sipId) {
        return Optional.ofNullable(store.get(sipId));
    }

    @Override
    public List<Sip> findByUserId(String userId) {
        return store.values().stream()
                .filter(sip -> sip.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Sip> findByState(SipState state) {
        return store.values().stream()
                .filter(sip -> sip.getState() == state)
                .collect(Collectors.toList());
    }

    @Override
    public List<Sip> findDueForExecution(LocalDate date) {
        return store.values().stream()
                .filter(sip -> sip.getState() == SipState.ACTIVE)
                .filter(sip -> !sip.getNextExecutionDate().isAfter(date))
                .collect(Collectors.toList());
    }

    @Override
    public void update(Sip sip) {
        store.put(sip.getId(), sip);
    }
}

