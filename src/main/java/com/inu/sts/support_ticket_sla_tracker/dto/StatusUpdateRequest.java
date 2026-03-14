package com.inu.sts.support_ticket_sla_tracker.dto;

import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotNull
    private TicketStatus status;
}
