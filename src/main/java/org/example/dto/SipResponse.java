package org.example.dto;

import lombok.Builder;
import lombok.Data;
import org.example.enums.SipMode;
import org.example.enums.SipState;
import org.example.model.Sip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SipResponse {

    private String id;
    private String userId;
    private String fundId;
    private BigDecimal amount;
    private BigDecimal baseAmount;
    private SipMode mode;
    private SipState state;
    private LocalDate startDate;
    private LocalDate nextExecutionDate;
    private double stepUpPercentage;
    private int installmentCount;
    private LocalDateTime createdAt;

    public static SipResponse from(Sip sip) {
        return SipResponse.builder()
                .id(sip.getId())
                .userId(sip.getUserId())
                .fundId(sip.getFundId())
                .amount(sip.getAmount())
                .baseAmount(sip.getBaseAmount())
                .mode(sip.getMode())
                .state(sip.getState())
                .startDate(sip.getStartDate())
                .nextExecutionDate(sip.getNextExecutionDate())
                .stepUpPercentage(sip.getStepUpPercentage())
                .installmentCount(sip.getInstallmentCount())
                .createdAt(sip.getCreatedAt())
                .build();
    }
}

