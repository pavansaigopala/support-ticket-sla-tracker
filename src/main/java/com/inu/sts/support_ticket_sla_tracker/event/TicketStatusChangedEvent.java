package com.inu.sts.support_ticket_sla_tracker.event;

import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TicketStatusChangedEvent {
    private final Ticket ticket;
    private final TicketStatus oldStatus;
    private final TicketStatus newStatus;
}
