package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.MutualFundResponse;
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
    public ResponseEntity<List<MutualFundResponse>> getAllFunds() {
        return ResponseEntity.ok(mutualFundService.getAllFunds().stream()
                .map(MutualFundResponse::from).toList());
    }

    @GetMapping("/{fundId}")
    public ResponseEntity<MutualFundResponse> getFundById(@PathVariable String fundId) {
        return ResponseEntity.ok(MutualFundResponse.from(mutualFundService.getFundById(fundId)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MutualFundResponse>> searchByCategory(@RequestParam String category) {
        return ResponseEntity.ok(mutualFundService.searchByCategory(category).stream()
                .map(MutualFundResponse::from).toList());
    }
}

