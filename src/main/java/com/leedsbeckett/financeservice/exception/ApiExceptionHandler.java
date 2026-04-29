package com.leedsbeckett.financeservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({AccountNotFoundException.class, InvoiceNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler({AccountNotValidException.class, InvoiceNotValidException.class})
    public ResponseEntity<Map<String, Object>> handleNotValidException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            RuntimeException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", exception.getMessage());
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
