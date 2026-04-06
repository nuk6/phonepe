package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.exception.FundError;
import org.example.exception.PhonePeRuntimeException;
import org.example.model.MutualFund;
import org.example.persistence.MutualFundDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MutualFundService {

    private final MutualFundDao mutualFundDao;

    public List<MutualFund> getAllFunds() {
        return mutualFundDao.findAll();
    }

    public MutualFund getFundById(String fundId) {
        return mutualFundDao.findById(fundId)
                .orElseThrow(() -> new PhonePeRuntimeException(FundError.FUND_NOT_FOUND));
    }

    public List<MutualFund> searchByCategory(String category) {
        return mutualFundDao.findAll().stream()
                .filter(fund -> fund.getCategory().name().equals(category))
                .collect(Collectors.toList());
    }

    public void addFund(MutualFund fund) {
        mutualFundDao.save(fund);
    }
}

