package com.zalo.clone.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses.
 * Ensures the API always returns {"error": "..."} format matching the Go version.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest()
                .body(Map.of("error", errors.toString()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();

        if (message != null) {
            if (message.contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("already exists") || message.contains("already taken")) {
                status = HttpStatus.CONFLICT;
            } else if (message.contains("invalid credentials")) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (message.contains("not authorized") || message.contains("only the")) {
                status = HttpStatus.FORBIDDEN;
            }
        }

        return ResponseEntity.status(status)
                .body(Map.of("error", message != null ? message : "internal server error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "internal server error"));
    }
}
