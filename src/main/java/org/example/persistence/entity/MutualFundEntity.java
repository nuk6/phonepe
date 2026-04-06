package org.example.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.MutualFund;
import org.example.model.MutualFundCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mutual_funds")
@Getter
@Setter
@NoArgsConstructor
public class MutualFundEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MutualFundCategory category;

    @Column(name = "current_nav", nullable = false)
    private BigDecimal currentNav;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static MutualFundEntity fromDomain(MutualFund fund) {
        MutualFundEntity entity = new MutualFundEntity();
        entity.setId(fund.getId());
        entity.setName(fund.getName());
        entity.setCategory(fund.getCategory());
        entity.setCurrentNav(fund.getCurrentNav());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public MutualFund toDomain() {
        return new MutualFund(id, name, category, currentNav);
    }
}

