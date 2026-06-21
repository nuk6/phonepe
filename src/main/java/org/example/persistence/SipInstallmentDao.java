package org.example.persistence;

import org.example.model.SipInstallment;

import java.util.List;

public interface SipInstallmentDao {

    void save(SipInstallment installment);

    void update(SipInstallment installment);

    List<SipInstallment> findBySipId(String sipId);
}

