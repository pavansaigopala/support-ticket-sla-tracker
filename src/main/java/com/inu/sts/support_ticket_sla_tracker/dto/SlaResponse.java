package com.inu.sts.support_ticket_sla_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaResponse {

    private Instant dueAt;
    private boolean breached;
    private Long remainingSeconds;
}
