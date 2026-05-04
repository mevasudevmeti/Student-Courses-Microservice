package com.lbu.student_service.exception;

/**
 * Represents a failure in another microservice, such as Finance or Library.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
