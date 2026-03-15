package com.inu.sts.support_ticket_sla_tracker.mapper;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import com.inu.sts.support_ticket_sla_tracker.dto.TicketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketMapperTest {

    private TicketMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TicketMapperImpl();
    }

    @Test
    void toResponse_mapsAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Ticket ticket = Ticket.builder()
                .id(id)
                .title("Login issue")
                .description("Cannot sign in")
                .priority(Priority.HIGH)
                .status(TicketStatus.IN_PROGRESS)
                .customerId("cust-1")
                .createdAt(now)
                .updatedAt(now)
                .version(1L)
                .slaBreached(false)
                .build();

        TicketResponse response = mapper.toResponse(ticket);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getTitle()).isEqualTo("Login issue");
        assertThat(response.getDescription()).isEqualTo("Cannot sign in");
        assertThat(response.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(response.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(response.getCustomerId()).isEqualTo("cust-1");
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
        assertThat(response.getVersion()).isEqualTo(1L);
        assertThat(response.getSlaBreached()).isFalse();
    }

    @Test
    void toResponse_nullTicket_returnsNull() {
        TicketResponse response = mapper.toResponse(null);
        assertThat(response).isNull();
    }
}
