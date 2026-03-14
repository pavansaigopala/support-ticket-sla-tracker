package com.inu.sts.support_ticket_sla_tracker.exception;

/**
 * Business rule violation (e.g. status transition not allowed).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
