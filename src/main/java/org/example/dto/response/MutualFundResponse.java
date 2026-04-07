package org.example.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.model.MutualFund;
import org.example.model.MutualFundCategory;

import java.math.BigDecimal;

@Data
@Builder
public class MutualFundResponse {

    private String id;
    private String name;
    private MutualFundCategory category;
    private BigDecimal currentNav;

    public static MutualFundResponse from(MutualFund fund) {
        return MutualFundResponse.builder()
                .id(fund.getId())
                .name(fund.getName())
                .category(fund.getCategory())
                .currentNav(fund.getCurrentNav())
                .build();
    }
}

