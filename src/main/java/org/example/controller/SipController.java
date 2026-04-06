package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.CreateSipRequest;
import org.example.dto.LumpSumRequest;
import org.example.model.Sip;
import org.example.model.SipInstallment;
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
    public ResponseEntity<Sip> createSip(@RequestBody CreateSipRequest request) {
        Sip sip = sipService.createSip(
                request.getUserId(), request.getFundId(), request.getAmount(),
                request.getMode(), request.getStartDate(), request.getStepUpPercentage()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(sip);
    }

    @PutMapping("/{sipId}/pause")
    public ResponseEntity<Sip> pauseSip(@PathVariable String sipId) {
        return ResponseEntity.ok(sipService.pauseSip(sipId));
    }

    @PutMapping("/{sipId}/unpause")
    public ResponseEntity<Sip> unpauseSip(@PathVariable String sipId) {
        return ResponseEntity.ok(sipService.unpauseSip(sipId));
    }

    @PutMapping("/{sipId}/stop")
    public ResponseEntity<Sip> stopSip(@PathVariable String sipId) {
        return ResponseEntity.ok(sipService.stopSip(sipId));
    }

    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<List<Sip>> getUserPortfolio(@PathVariable String userId) {
        return ResponseEntity.ok(sipService.getUserPortfolio(userId));
    }

    @PostMapping("/{sipId}/lumpsum")
    public ResponseEntity<List<SipInstallment>> lumpSumPayment(
            @PathVariable String sipId, @RequestBody LumpSumRequest request) {
        return ResponseEntity.ok(sipService.lumpSumPayment(sipId, request.getMissedInstallments()));
    }

    @PostMapping("/execute")
    public ResponseEntity<List<SipInstallment>> executeDueSips(
            @RequestParam(required = false) LocalDate date) {
        LocalDate executionDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(executionService.executeAllDueSips(executionDate));
    }
}

