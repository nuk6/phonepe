package org.example.persistence.inmemory;

import org.example.model.SipInstallment;
import org.example.persistence.SipInstallmentDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemorySipInstallmentDao implements SipInstallmentDao {

    private final Map<String, SipInstallment> store = new ConcurrentHashMap<>();

    @Override
    public void save(SipInstallment installment) {
        store.put(installment.getId(), installment);
    }

    @Override
    public List<SipInstallment> findBySipId(String sipId) {
        return store.values().stream()
                .filter(inst -> inst.getSipId().equals(sipId))
                .collect(Collectors.toList());
    }
}

