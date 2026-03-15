package com.inu.sts.support_ticket_sla_tracker.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    void hasMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Ticket not found: 123");
        assertThat(ex.getMessage()).isEqualTo("Ticket not found: 123");
    }
}
