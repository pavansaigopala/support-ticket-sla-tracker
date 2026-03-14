package com.inu.sts.support_ticket_sla_tracker.dto;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
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
public class TicketResponse {

    private UUID id;
    private String title;
    private String description;
    private Priority priority;
    private TicketStatus status;
    private String customerId;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    private Boolean slaBreached;
}
