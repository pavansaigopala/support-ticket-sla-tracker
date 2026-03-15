package com.inu.sts.support_ticket_sla_tracker.event;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditEventPayloadTest {

    @Test
    void constructor_setsFields() {
        Object source = new Object();
        UUID entityId = UUID.randomUUID();
        AuditEventPayload event = new AuditEventPayload(
                source, "TICKET", entityId, "TICKET_CREATED", "title=Test", "corr-123");

        assertThat(event.getSource()).isSameAs(source);
        assertThat(event.getEntityType()).isEqualTo("TICKET");
        assertThat(event.getEntityId()).isEqualTo(entityId);
        assertThat(event.getEventType()).isEqualTo("TICKET_CREATED");
        assertThat(event.getPayload()).isEqualTo("title=Test");
        assertThat(event.getCorrelationId()).isEqualTo("corr-123");
    }

    @Test
    void constructor_nullPayloadAndCorrelationId_becomeEmptyString() {
        UUID entityId = UUID.randomUUID();
        AuditEventPayload event = new AuditEventPayload(
                this, "TICKET", entityId, "STATUS_CHANGED", null, null);

        assertThat(event.getPayload()).isEmpty();
        assertThat(event.getCorrelationId()).isEmpty();
    }
}
