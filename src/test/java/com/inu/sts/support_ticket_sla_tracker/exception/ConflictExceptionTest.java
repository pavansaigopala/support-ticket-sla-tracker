package com.inu.sts.support_ticket_sla_tracker.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConflictExceptionTest {

    @Test
    void hasMessage() {
        ConflictException ex = new ConflictException("Version conflict");
        assertThat(ex.getMessage()).isEqualTo("Version conflict");
    }
}
