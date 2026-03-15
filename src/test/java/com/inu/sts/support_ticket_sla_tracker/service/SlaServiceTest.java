package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import com.inu.sts.support_ticket_sla_tracker.exception.ResourceNotFoundException;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private SlaService slaService;

    @Test
    void getSla_whenTicketNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slaService.getSla(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void getSla_whenTicketExists_returnsResponse() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Ticket ticket = Ticket.builder()
                .id(id)
                .title("Test")
                .priority(Priority.HIGH)
                .status(TicketStatus.NEW)
                .customerId("c1")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .version(0L)
                .build();
        when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));

        var result = slaService.getSla(id);

        assertThat(result).isNotNull();
        assertThat(result.getDueAt()).isAfter(createdAt);
        assertThat(result.getRemainingSeconds()).isNotNull();
    }
}
