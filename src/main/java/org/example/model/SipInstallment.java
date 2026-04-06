package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@ToString
@AllArgsConstructor
public class SipInstallment {

    private final String id;
    private final String sipId;
    private final BigDecimal amount;
    private final BigDecimal nav;
    private final BigDecimal unitsAllotted;
    private final LocalDate executionDate;
    @Setter
    private InstallmentStatus status;
}

