package org.example.dto;

import lombok.Data;
import org.example.enums.SipMode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSipRequest {

    private String userId;
    private String fundId;
    private BigDecimal amount;
    private SipMode mode;
    private LocalDate startDate;
    private double stepUpPercentage;
}

