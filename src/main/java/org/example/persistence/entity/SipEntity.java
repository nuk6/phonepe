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
import org.example.enums.SipMode;
import org.example.enums.SipState;
import org.example.model.Sip;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sips")
@Getter
@Setter
@NoArgsConstructor
public class SipEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "fund_id", nullable = false)
    private String fundId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "base_amount", nullable = false)
    private BigDecimal baseAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SipMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SipState state;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_execution_date", nullable = false)
    private LocalDate nextExecutionDate;

    @Column(name = "step_up_percentage", nullable = false)
    private double stepUpPercentage;

    @Column(name = "installment_count", nullable = false)
    private int installmentCount;

    public static SipEntity fromDomain(Sip sip) {
        SipEntity e = new SipEntity();
        e.setId(sip.getId());
        e.setUserId(sip.getUserId());
        e.setFundId(sip.getFundId());
        e.setAmount(sip.getAmount());
        e.setBaseAmount(sip.getBaseAmount());
        e.setMode(sip.getMode());
        e.setState(sip.getState());
        e.setStartDate(sip.getStartDate());
        e.setNextExecutionDate(sip.getNextExecutionDate());
        e.setStepUpPercentage(sip.getStepUpPercentage());
        e.setInstallmentCount(sip.getInstallmentCount());
        return e;
    }

    public Sip toDomain() {
        Sip sip = new Sip(id, userId, fundId, amount, mode, startDate, stepUpPercentage);
        sip.setAmount(this.amount);
        sip.setState(this.state);
        sip.setNextExecutionDate(this.nextExecutionDate);
        sip.setInstallmentCount(this.installmentCount);
        return sip;
    }
}

