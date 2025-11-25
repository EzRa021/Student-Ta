package com.lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for creating a reply to a request with validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCreateDto {

    @NotBlank(message = "Reply message cannot be empty")
    @Length(min = 1, max = 5000, message = "Reply must be between 1 and 5000 characters")
    private String message;
}
