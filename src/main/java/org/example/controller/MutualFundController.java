package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.model.MutualFund;
import org.example.service.MutualFundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
public class MutualFundController {

    private final MutualFundService mutualFundService;

    @GetMapping
    public ResponseEntity<List<MutualFund>> getAllFunds() {
        return ResponseEntity.ok(mutualFundService.getAllFunds());
    }

    @GetMapping("/{fundId}")
    public ResponseEntity<MutualFund> getFundById(@PathVariable String fundId) {
        return ResponseEntity.ok(mutualFundService.getFundById(fundId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MutualFund>> searchByCategory(@RequestParam String category) {
        return ResponseEntity.ok(mutualFundService.searchByCategory(category));
    }
}

