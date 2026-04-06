package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.CreateSipRequest;
import org.example.dto.InstallmentResponse;
import org.example.dto.LumpSumRequest;
import org.example.dto.SipResponse;
import org.example.service.SipExecutionService;
import org.example.service.SipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sips")
@RequiredArgsConstructor
public class SipController {

    private final SipService sipService;
    private final SipExecutionService executionService;

    @PostMapping
    public ResponseEntity<SipResponse> createSip(@Valid @RequestBody CreateSipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SipResponse.from(sipService.createSip(
                        request.getUserId(), request.getFundId(), request.getAmount(),
                        request.getMode(), request.getStartDate(), request.getStepUpPercentage()
                )));
    }

    @PutMapping("/{sipId}/pause")
    public ResponseEntity<SipResponse> pauseSip(@PathVariable String sipId) {
        return ResponseEntity.ok(SipResponse.from(sipService.pauseSip(sipId)));
    }

    @PutMapping("/{sipId}/unpause")
    public ResponseEntity<SipResponse> unpauseSip(@PathVariable String sipId) {
        return ResponseEntity.ok(SipResponse.from(sipService.unpauseSip(sipId)));
    }

    @PutMapping("/{sipId}/stop")
    public ResponseEntity<SipResponse> stopSip(@PathVariable String sipId) {
        return ResponseEntity.ok(SipResponse.from(sipService.stopSip(sipId)));
    }

    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<List<SipResponse>> getUserPortfolio(@PathVariable String userId) {
        return ResponseEntity.ok(sipService.getUserPortfolio(userId).stream()
                .map(SipResponse::from).toList());
    }

    @PostMapping("/{sipId}/lumpsum")
    public ResponseEntity<List<InstallmentResponse>> lumpSumPayment(
            @PathVariable String sipId, @Valid @RequestBody LumpSumRequest request) {
        return ResponseEntity.ok(sipService.lumpSumPayment(sipId, request.getMissedInstallments())
                .stream().map(InstallmentResponse::from).toList());
    }

    @PostMapping("/execute")
    public ResponseEntity<List<InstallmentResponse>> executeDueSips(
            @RequestParam(required = false) LocalDate date) {
        LocalDate executionDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(executionService.executeAllDueSips(executionDate)
                .stream().map(InstallmentResponse::from).toList());
    }
}

