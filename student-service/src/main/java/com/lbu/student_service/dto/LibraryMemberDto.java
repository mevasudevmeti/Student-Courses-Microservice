package com.lbu.student_service.dto;

import lombok.Data;

/**
 * DTO used when creating a matching member account in the Library microservice.
 */
@Data
public class LibraryMemberDto {
    private Long id;
    private String memberCode;
    private String fullName;
    private String email;
    private String status;
}
