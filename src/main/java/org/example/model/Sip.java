package org.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.enums.SipMode;
import org.example.enums.SipState;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@ToString
public class Sip {

    private final String id;
    private final String userId;
    private final String fundId;
    @Setter
    private BigDecimal amount;
    private final BigDecimal baseAmount;
    private final SipMode mode;
    @Setter
    private SipState state;
    private final LocalDate startDate;
    @Setter
    private LocalDate nextExecutionDate;
    private final double stepUpPercentage;
    @Setter
    private int installmentCount;

    public Sip(String id, String userId, String fundId, BigDecimal amount,
               SipMode mode, LocalDate startDate, double stepUpPercentage) {
        this.id = id;
        this.userId = userId;
        this.fundId = fundId;
        this.amount = amount;
        this.baseAmount = amount;
        this.mode = mode;
        this.state = SipState.ACTIVE;
        this.startDate = startDate;
        this.nextExecutionDate = startDate;
        this.stepUpPercentage = stepUpPercentage;
        this.installmentCount = 0;
    }

    public void incrementInstallmentCount() {
        this.installmentCount++;
    }
}

