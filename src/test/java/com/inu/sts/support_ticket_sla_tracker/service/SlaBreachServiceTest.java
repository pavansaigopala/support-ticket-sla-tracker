package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import com.inu.sts.support_ticket_sla_tracker.event.AuditEventPublisher;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaBreachServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private AuditEventPublisher auditEventPublisher;

    @InjectMocks
    private SlaBreachService slaBreachService;

    private Ticket breachedTicket;
    private Ticket notYetBreachedTicket;

    @BeforeEach
    void setUp() {
        // HIGH = 24h; created 25h ago -> breached
        Instant old = Instant.now().minusSeconds(25 * 3600);
        breachedTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .title("Old")
                .priority(Priority.HIGH)
                .status(TicketStatus.NEW)
                .customerId("c1")
                .createdAt(old)
                .updatedAt(old)
                .version(0L)
                .slaBreached(false)
                .build();

        // CRITICAL = 4h; created 2h ago -> not breached
        Instant recent = Instant.now().minusSeconds(2 * 3600);
        notYetBreachedTicket = Ticket.builder()
                .id(UUID.randomUUID())
                .title("Recent")
                .priority(Priority.CRITICAL)
                .status(TicketStatus.IN_PROGRESS)
                .customerId("c2")
                .createdAt(recent)
                .updatedAt(recent)
                .version(0L)
                .slaBreached(false)
                .build();
    }

    @Test
    void markBreachedAndPublishEvents_whenNoCandidates_returnsZero() {
        when(ticketRepository.findByStatusNotInAndSlaBreachedFalse(anyList())).thenReturn(List.of());

        int count = slaBreachService.markBreachedAndPublishEvents();

        assertThat(count).isZero();
        verify(ticketRepository).findByStatusNotInAndSlaBreachedFalse(List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
    }

    @Test
    void markBreachedAndPublishEvents_whenOneBreached_marksAndPublishes() {
        when(ticketRepository.findByStatusNotInAndSlaBreachedFalse(anyList())).thenReturn(List.of(breachedTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        int count = slaBreachService.markBreachedAndPublishEvents();

        assertThat(count).isEqualTo(1);
        ArgumentCaptor<Ticket> saved = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(saved.capture());
        assertThat(saved.getValue().getSlaBreached()).isTrue();
        verify(auditEventPublisher).publish(eq("TICKET"), eq(breachedTicket.getId()), eq("SLA_BREACHED"), anyString());
    }

    @Test
    void markBreachedAndPublishEvents_whenOneNotYetBreached_doesNotMark() {
        when(ticketRepository.findByStatusNotInAndSlaBreachedFalse(anyList())).thenReturn(List.of(notYetBreachedTicket));

        int count = slaBreachService.markBreachedAndPublishEvents();

        assertThat(count).isZero();
        verify(ticketRepository, org.mockito.Mockito.never()).save(any());
        verify(auditEventPublisher, org.mockito.Mockito.never()).publish(anyString(), any(), anyString(), anyString());
    }

    @Test
    void markBreachedAndPublishEvents_mixedCandidates_marksOnlyBreached() {
        when(ticketRepository.findByStatusNotInAndSlaBreachedFalse(anyList()))
                .thenReturn(List.of(breachedTicket, notYetBreachedTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        int count = slaBreachService.markBreachedAndPublishEvents();

        assertThat(count).isEqualTo(1);
        verify(ticketRepository).save(breachedTicket);
        verify(auditEventPublisher).publish(eq("TICKET"), eq(breachedTicket.getId()), eq("SLA_BREACHED"), anyString());
    }
}
