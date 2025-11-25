package com.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaResponse {

    private String id;
    private String username;
    private String email;
    private String studentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long pendingAssignments;
    private long resolvedAssignments;
    private long totalAssignments;
}


