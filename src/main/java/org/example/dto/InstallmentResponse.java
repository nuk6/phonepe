package org.example.dto;

import lombok.Builder;
import lombok.Data;
import org.example.enums.InstallmentStatus;
import org.example.model.SipInstallment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class InstallmentResponse {

    private String id;
    private String sipId;
    private BigDecimal amount;
    private BigDecimal nav;
    private BigDecimal unitsAllotted;
    private LocalDate executionDate;
    private InstallmentStatus status;

    public static InstallmentResponse from(SipInstallment inst) {
        return InstallmentResponse.builder()
                .id(inst.getId())
                .sipId(inst.getSipId())
                .amount(inst.getAmount())
                .nav(inst.getNav())
                .unitsAllotted(inst.getUnitsAllotted())
                .executionDate(inst.getExecutionDate())
                .status(inst.getStatus())
                .build();
    }
}

