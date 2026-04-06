package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.ErrorResponse;
import org.example.exception.PhonePeRuntimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PhonePeRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(PhonePeRuntimeException ex) {
        org.example.exception.Error error = ex.getError();
        log.warn("Business error: code={}, status={}, message={}",
                error.getCode(), error.getStatus(), error.getDescription());
        ErrorResponse body = new ErrorResponse(error.getStatus(), error.getCode(), error.getDescription());
        return ResponseEntity.status(error.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        ErrorResponse body = new ErrorResponse(400, "VALIDATION_ERROR", message);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception caught", ex);
        ErrorResponse body = new ErrorResponse(500, "INTERNAL_ERROR", "Something went wrong. Please try again.");
        return ResponseEntity.status(500).body(body);
    }
}

