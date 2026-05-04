package com.lbu.student_service.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO used to receive Finance account data without exposing or depending on Finance entity classes.
 */
@Data
public class AccountDto {
    private Long id;
    private Long studentId;
    private Double balance;
    private LocalDate dateCreated;

    public boolean hasOutstandingBalance() {
        return balance != null && balance > 0;
    }
}
