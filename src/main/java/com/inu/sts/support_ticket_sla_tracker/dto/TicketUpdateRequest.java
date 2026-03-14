package com.inu.sts.support_ticket_sla_tracker.dto;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateRequest {

    @Size(min = 5, max = 120)
    private String title;

    @Size(max = 2000)
    private String description;

    private Priority priority;
}
