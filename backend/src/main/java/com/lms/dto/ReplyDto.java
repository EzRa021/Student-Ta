package com.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for reply operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDto {

    private String id;
    private String requestId;
    private String taId;
    private String taUsername;
    private String message;
    private LocalDateTime createdAt;
}


