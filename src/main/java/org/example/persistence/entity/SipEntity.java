package org.example.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.enums.SipMode;
import org.example.model.enums.SipState;
import org.example.model.Sip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Version
    private int version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
        e.setVersion(sip.getVersion());
        e.setCreatedAt(sip.getCreatedAt());
        e.setUpdatedAt(LocalDateTime.now());
        return e;
    }

    public Sip toDomain() {
        Sip sip = new Sip(id, userId, fundId, amount, mode, startDate, stepUpPercentage);
        sip.setAmount(this.amount);
        sip.setState(this.state);
        sip.setNextExecutionDate(this.nextExecutionDate);
        sip.setInstallmentCount(this.installmentCount);
        sip.setVersion(this.version);
        sip.setCreatedAt(this.createdAt);
        sip.setUpdatedAt(this.updatedAt);
        return sip;
    }
}

