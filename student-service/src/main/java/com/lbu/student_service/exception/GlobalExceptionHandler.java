package com.lbu.student_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<APIError> handleStudentNotFoundException(StudentNotFoundException ex) {
       // return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);

              APIError apiError = new APIError(
                        LocalDateTime.now(),
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND);
              return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

}