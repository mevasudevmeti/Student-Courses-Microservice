package com.lbu.student_service.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class APIError {
    private LocalDateTime timestamp;
    private String errorMessage;
    private HttpStatus statusCode;

//    public APIError() {
//        this.timestamp = LocalDateTime.now();
//    }
    public APIError(LocalDateTime timestamp, String error, HttpStatus statusCode) {
        this.timestamp = timestamp;
        this.errorMessage = error;
        this.statusCode = statusCode;
    }
}