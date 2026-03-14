package com.inu.sts.support_ticket_sla_tracker.dto;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCreateRequest {

    @NotBlank
    @Size(min = 5, max = 120)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull
    private Priority priority;

    @NotBlank
    @Size(max = 255)
    private String customerId;
}
