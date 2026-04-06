package org.example.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.enums.InstallmentStatus;
import org.example.model.SipInstallment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sip_installments")
@Getter
@Setter
@NoArgsConstructor
public class SipInstallmentEntity {

    @Id
    private String id;

    @Column(name = "sip_id", nullable = false)
    private String sipId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal nav;

    @Column(name = "units_allotted", nullable = false)
    private BigDecimal unitsAllotted;

    @Column(name = "execution_date", nullable = false)
    private LocalDate executionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentStatus status;

    public static SipInstallmentEntity fromDomain(SipInstallment inst) {
        SipInstallmentEntity e = new SipInstallmentEntity();
        e.setId(inst.getId());
        e.setSipId(inst.getSipId());
        e.setAmount(inst.getAmount());
        e.setNav(inst.getNav());
        e.setUnitsAllotted(inst.getUnitsAllotted());
        e.setExecutionDate(inst.getExecutionDate());
        e.setStatus(inst.getStatus());
        return e;
    }

    public SipInstallment toDomain() {
        return new SipInstallment(id, sipId, amount, nav, unitsAllotted, executionDate, status);
    }
}

