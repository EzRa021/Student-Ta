package com.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {

    private long pendingCount;
    private long inProgressCount;
    private long resolvedCount;
    private long cancelledCount;
    private Double averageWaitTimeSeconds;
    private long totalRequests;
}