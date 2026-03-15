package com.inu.sts.support_ticket_sla_tracker.event;

import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TicketCreatedEvent {
    private final Ticket ticket;
}
