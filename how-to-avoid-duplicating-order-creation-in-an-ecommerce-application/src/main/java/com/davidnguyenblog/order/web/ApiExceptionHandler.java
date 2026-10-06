package com.davidnguyenblog.order.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    ResponseEntity<Map<String, String>> conflict(IdempotencyConflictException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RequestInProgressException.class)
    ResponseEntity<Map<String, String>> inProgress(RequestInProgressException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    ResponseEntity<Map<String, String>> badToken(InvalidTokenException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
