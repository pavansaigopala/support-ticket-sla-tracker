package com.inu.sts.support_ticket_sla_tracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

    private String code;
    private String message;
    private Instant timestamp;
    private List<FieldError> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message, Instant timestamp, List<FieldError> errors) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.errors = errors;
    }

    public static ErrorResponse of(String code, String message, Instant timestamp) {
        return new ErrorResponse(code, message, timestamp, null);
    }

    public static ErrorResponse of(String code, String message, Instant timestamp, List<FieldError> errors) {
        return new ErrorResponse(code, message, timestamp, errors);
    }

    @Data
    public static class FieldError {
        private String field;
        private String message;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
