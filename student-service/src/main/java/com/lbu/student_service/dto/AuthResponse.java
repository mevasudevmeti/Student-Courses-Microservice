package com.lbu.student_service.dto;

import com.lbu.student_service.entities.Student;
import com.lbu.student_service.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after login so the UI knows the user role and student profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String username;
    private String role;
    private Student student;

    public static AuthResponse from(User user, Student student) {
        return new AuthResponse(user.getUserId(), user.getUsername(), user.getRole(), student);
    }
}
