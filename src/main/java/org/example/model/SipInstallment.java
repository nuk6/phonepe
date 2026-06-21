package org.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.model.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
public class SipInstallment {

    private final String id;
    private final String sipId;
    private final BigDecimal amount;
    private final BigDecimal nav;
    @Setter
    private BigDecimal unitsAllotted;
    private final LocalDate executionDate;
    @Setter
    private InstallmentStatus status;
    private final String idempotencyKey;
    @Setter
    private LocalDateTime createdAt;

    public SipInstallment(String id, String sipId, BigDecimal amount,
                          BigDecimal nav, BigDecimal unitsAllotted,
                          LocalDate executionDate, InstallmentStatus status,
                          String idempotencyKey) {
        this.id = id;
        this.sipId = sipId;
        this.amount = amount;
        this.nav = nav;
        this.unitsAllotted = unitsAllotted;
        this.executionDate = executionDate;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
    }
}

