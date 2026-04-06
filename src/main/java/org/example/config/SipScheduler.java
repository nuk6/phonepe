package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.SipInstallment;
import org.example.service.SipExecutionService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@Profile({"local", "qa", "prod"})
@RequiredArgsConstructor
public class SipScheduler {

    private final SipExecutionService executionService;

    // runs every day at 6 AM
    @Scheduled(cron = "${sip.execution.cron:0 0 6 * * *}")
    public void executeDueSips() {
        LocalDate today = LocalDate.now();
        log.info("Scheduled SIP execution triggered for {}", today);

        List<SipInstallment> results = executionService.executeAllDueSips(today);
        log.info("Executed {} SIPs for {}", results.size(), today);
    }
}

