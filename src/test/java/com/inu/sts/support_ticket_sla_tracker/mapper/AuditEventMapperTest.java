package com.inu.sts.support_ticket_sla_tracker.mapper;

import com.inu.sts.support_ticket_sla_tracker.domain.AuditEvent;
import com.inu.sts.support_ticket_sla_tracker.dto.AuditEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEventMapperTest {

    private AuditEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuditEventMapperImpl();
    }

    @Test
    void toResponse_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        Instant now = Instant.now();
        AuditEvent event = AuditEvent.builder()
                .id(id)
                .entityType("TICKET")
                .entityId(entityId)
                .eventType("TICKET_CREATED")
                .payload("{\"title\":\"Test\"}")
                .createdAt(now)
                .build();

        AuditEventResponse response = mapper.toResponse(event);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getEntityType()).isEqualTo("TICKET");
        assertThat(response.getEntityId()).isEqualTo(entityId);
        assertThat(response.getEventType()).isEqualTo("TICKET_CREATED");
        assertThat(response.getPayload()).isEqualTo("{\"title\":\"Test\"}");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_nullEvent_returnsNull() {
        AuditEventResponse response = mapper.toResponse(null);
        assertThat(response).isNull();
    }
}
