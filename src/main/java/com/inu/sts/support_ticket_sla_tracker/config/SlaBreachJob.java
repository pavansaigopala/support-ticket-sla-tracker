package com.inu.sts.support_ticket_sla_tracker.config;

import com.inu.sts.support_ticket_sla_tracker.service.SlaBreachService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs every minute (configurable via sla.breach.cron). Finds SLA-breached tickets and writes one SLA_BREACHED audit event each.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sla.breach.job.enabled", havingValue = "true", matchIfMissing = true)
public class SlaBreachJob {

    private static final Logger log = LoggerFactory.getLogger(SlaBreachJob.class);

    private final SlaBreachService slaBreachService;

    @Scheduled(cron = "${sla.breach.cron:0 * * * * ?}")  // every minute at second 0
    public void run() {
        log.debug("SLA breach job started");
        int marked = slaBreachService.markBreachedAndPublishEvents();
        log.debug("SLA breach job finished, marked {} ticket(s)", marked);
    }
}
