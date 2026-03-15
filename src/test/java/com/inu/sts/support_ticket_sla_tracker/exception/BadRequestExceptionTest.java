package com.inu.sts.support_ticket_sla_tracker.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BadRequestExceptionTest {

    @Test
    void hasMessage() {
        BadRequestException ex = new BadRequestException("Cannot change status from CLOSED");
        assertThat(ex.getMessage()).isEqualTo("Cannot change status from CLOSED");
    }
}
