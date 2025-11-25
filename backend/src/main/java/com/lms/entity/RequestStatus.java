package com.lms.entity;

/**
 * Request status enumeration.
 * PENDING - Newly created request awaiting TA assignment
 * IN_PROGRESS - TA has claimed and is working on the request
 * RESOLVED - Request has been completed
 * CANCELLED - Request was cancelled (by admin or student)
 */
public enum RequestStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED,
    CANCELLED
}