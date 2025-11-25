package com.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating student accounts via admin.
 */
@Data
public class StudentUpdateRequest {

    @Size(min = 3, max = 50)
    private String username;

    @Email
    private String email;

    @Size(max = 50)
    private String studentId;

    @Size(min = 6, max = 100)
    private String password;
}



