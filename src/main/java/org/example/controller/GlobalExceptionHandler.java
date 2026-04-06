package org.example.controller;

import org.example.dto.ErrorResponse;
import org.example.exception.PhonePeRuntimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PhonePeRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(PhonePeRuntimeException ex) {
        org.example.exception.Error error = ex.getError();
        ErrorResponse body = new ErrorResponse(error.getStatus(), error.getCode(), error.getDescription());
        return ResponseEntity.status(error.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse body = new ErrorResponse(500, "INTERNAL_ERROR", ex.getMessage());
        return ResponseEntity.status(500).body(body);
    }
}

