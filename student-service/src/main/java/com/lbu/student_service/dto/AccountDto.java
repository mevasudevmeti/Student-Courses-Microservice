package com.lbu.student_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class AccountDto {
    private String studentId;

    // Ensure this exact name is used
    private boolean hasOutstandingBalance;

    // Standard getters/setters
    // Note: for booleans, the getter is usually 'is...'
    public boolean isHasOutstandingBalance() {
        return hasOutstandingBalance;
    }

    public void setHasOutstandingBalance(boolean hasOutstandingBalance) {
        this.hasOutstandingBalance = hasOutstandingBalance;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}