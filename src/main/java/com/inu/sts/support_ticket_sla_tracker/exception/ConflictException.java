package com.inu.sts.support_ticket_sla_tracker.exception;

/**
 * Used for optimistic locking conflicts (409).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
