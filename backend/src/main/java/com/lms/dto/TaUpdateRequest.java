package com.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaUpdateRequest {

    @Size(min = 3, max = 50)
    private String username;

    @Email
    private String email;

    @Size(min = 6, max = 100)
    private String password;

    private String studentId;
}


