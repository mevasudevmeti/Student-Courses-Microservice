package com.lbu.student_service.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO sent to the Finance microservice when enrolment or library fine invoices are created.
 */
@Data
public class CreateInvoiceRequest {
    private Long studentId;
    private String description;
    private Double amount;
    private LocalDate dateCreated;
}
