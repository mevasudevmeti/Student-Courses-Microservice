package com.lbu.student_service.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO used to receive invoice references from the Finance microservice.
 */
@Data
public class InvoiceDto {
    private Long id;
    private Long studentId;
    private String description;
    private Double amount;
    private String status;
    private LocalDate dateCreated;
}
