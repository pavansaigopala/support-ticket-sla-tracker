package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import com.inu.sts.support_ticket_sla_tracker.dto.SlaResponse;
import com.inu.sts.support_ticket_sla_tracker.exception.ResourceNotFoundException;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlaService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public SlaResponse getSla(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        Instant now = Instant.now();
        Instant dueAt = SlaCalculator.dueAt(ticket.getCreatedAt(), ticket.getPriority());
        boolean resolvedOrClosed = ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED;
        boolean breached = !resolvedOrClosed && now.isAfter(dueAt);
        long remainingSeconds = SlaCalculator.remainingSeconds(dueAt, now);

        return SlaResponse.builder()
                .dueAt(dueAt)
                .breached(breached)
                .remainingSeconds(remainingSeconds)
                .build();
    }
}
