package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@AllArgsConstructor
public class MutualFund {

    private final String id;
    private final String name;
    private final MutualFundCategory category;
    @Setter
    private BigDecimal currentNav;
}

