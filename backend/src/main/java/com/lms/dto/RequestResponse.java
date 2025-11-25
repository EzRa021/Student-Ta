package com.lms.dto;

import com.lms.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a help request for API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponse {

    private String id;
    private String title;
    private String description;
    private String studentId;
    private String studentUsername;
    private String labSessionId;
    private RequestStatus status;
    private Long priority;
    private String assignedTo;
    private String assignedToUsername;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String metadata;
}
