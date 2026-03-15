package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import com.inu.sts.support_ticket_sla_tracker.event.AuditEventPublisher;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Finds tickets that are SLA breached (due passed, not RESOLVED/CLOSED), marks them, and writes one SLA_BREACHED audit event per ticket.
 */
@Service
@RequiredArgsConstructor
public class SlaBreachService {

    private static final Logger log = LoggerFactory.getLogger(SlaBreachService.class);

    private final TicketRepository ticketRepository;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public int markBreachedAndPublishEvents() {
        List<Ticket> candidates = ticketRepository.findByStatusNotInAndSlaBreachedFalse(
                List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
        Instant now = Instant.now();
        int count = 0;
        for (Ticket ticket : candidates) {
            Instant dueAt = SlaCalculator.dueAt(ticket.getCreatedAt(), ticket.getPriority());
            if (now.isAfter(dueAt)) {
                ticket.setSlaBreached(true);
                ticketRepository.save(ticket);
                auditEventPublisher.publish("TICKET", ticket.getId(), "SLA_BREACHED",
                        "dueAt=" + dueAt + ",priority=" + ticket.getPriority());
                count++;
            }
        }
        if (count > 0) {
            log.info("SLA breach job: marked {} ticket(s) as breached", count);
        }
        return count;
    }
}
