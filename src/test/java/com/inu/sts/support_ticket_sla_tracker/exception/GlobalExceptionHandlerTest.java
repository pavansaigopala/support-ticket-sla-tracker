package com.inu.sts.support_ticket_sla_tracker.exception;

import com.inu.sts.support_ticket_sla_tracker.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_returns404WithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Ticket not found: xyz");
        ResponseEntity<ErrorResponse> result = handler.handleNotFound(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCode()).isEqualTo("NOT_FOUND");
        assertThat(result.getBody().getMessage()).isEqualTo("Ticket not found: xyz");
    }

    @Test
    void handleBadRequest_returns400WithMessage() {
        BadRequestException ex = new BadRequestException("RESOLVED requires at least one comment");
        ResponseEntity<ErrorResponse> result = handler.handleBadRequest(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCode()).isEqualTo("BAD_REQUEST");
        assertThat(result.getBody().getMessage()).isEqualTo("RESOLVED requires at least one comment");
    }

    @Test
    void handleConflict_returns409WithMessage() {
        ConflictException ex = new ConflictException("Version conflict");
        ResponseEntity<ErrorResponse> result = handler.handleConflict(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCode()).isEqualTo("CONFLICT");
        assertThat(result.getBody().getMessage()).isEqualTo("Version conflict");
    }

    @Test
    void handleOptimisticLock_returns409WithFixedMessage() {
        ObjectOptimisticLockingFailureException ex = new ObjectOptimisticLockingFailureException(Object.class, 1L);
        ResponseEntity<ErrorResponse> result = handler.handleOptimisticLock(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCode()).isEqualTo("CONFLICT");
        assertThat(result.getBody().getMessage()).contains("updated by another request");
    }

    @Test
    void handleValidation_returns400WithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("ticket", "title", "size must be between 5 and 120")));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> result = handler.handleValidation(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(result.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(result.getBody().getErrors()).hasSize(1);
        assertThat(result.getBody().getErrors().get(0).getField()).isEqualTo("title");
        assertThat(result.getBody().getErrors().get(0).getMessage()).contains("5 and 120");
    }

    @Test
    void handleOther_returns500WithGenericMessage() {
        Exception ex = new RuntimeException("Unexpected");
        ResponseEntity<ErrorResponse> result = handler.handleOther(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(result.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}
