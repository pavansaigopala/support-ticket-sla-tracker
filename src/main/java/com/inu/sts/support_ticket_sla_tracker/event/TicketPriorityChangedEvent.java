package com.inu.sts.support_ticket_sla_tracker.event;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TicketPriorityChangedEvent {
    private final Ticket ticket;
    private final Priority oldPriority;
    private final Priority newPriority;
}
