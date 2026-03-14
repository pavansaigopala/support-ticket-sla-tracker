package com.inu.sts.support_ticket_sla_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventResponse {

    private UUID id;
    private String entityType;
    private UUID entityId;
    private String eventType;
    private String payload;
    private Instant createdAt;
}
