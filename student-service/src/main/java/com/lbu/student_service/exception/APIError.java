package com.lbu.student_service.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Standard error response returned by the REST API when validation, lookup or integration errors occur.
 */
@Data
public class APIError {
    private final LocalDateTime timestamp;
    private final String errorMessage;
    private final HttpStatus statusCode;
}
