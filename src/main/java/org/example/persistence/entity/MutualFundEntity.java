package org.example.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.MutualFund;

import java.math.BigDecimal;

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

    @Column(nullable = false)
    private String category;

    @Column(name = "current_nav", nullable = false)
    private BigDecimal currentNav;

    public static MutualFundEntity fromDomain(MutualFund fund) {
        MutualFundEntity entity = new MutualFundEntity();
        entity.setId(fund.getId());
        entity.setName(fund.getName());
        entity.setCategory(fund.getCategory());
        entity.setCurrentNav(fund.getCurrentNav());
        return entity;
    }

    public MutualFund toDomain() {
        return new MutualFund(id, name, category, currentNav);
    }
}

