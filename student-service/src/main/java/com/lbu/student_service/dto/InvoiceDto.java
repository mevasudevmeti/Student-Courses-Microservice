package com.lbu.student_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InvoiceDto {
    private Long id;
    private String reference; // Finance uses an 8-character random string
    private Double amount;
    private LocalDate dueDate;
    private Type type;
    private Status status;

    // This allows you to send the studentId directly in the JSON
    // because Finance uses @JsonProperty getStudentId()
    private String studentId;
}