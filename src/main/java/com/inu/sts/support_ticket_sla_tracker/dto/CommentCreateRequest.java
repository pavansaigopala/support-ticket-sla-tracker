package com.inu.sts.support_ticket_sla_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateRequest {

    @NotBlank
    @Size(min = 1, max = 1000)
    private String comment;

    @NotBlank
    @Size(max = 255)
    private String author;
}
