package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request entity representing student help requests.
 * Priority is set to created_at epoch by default for FCFS ordering.
 * TAs can manually adjust priority for re-ordering.
 */
@Entity
@Table(name = "requests", indexes = {
    @Index(name = "idx_status_priority", columnList = "status,priority,created_at"),
    @Index(name = "idx_student_id", columnList = "student_id"),
    @Index(name = "idx_assigned_to", columnList = "assigned_to")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "student_id", nullable = false, length = 36)
    private String studentId;

    @Column(name = "lab_session_id", length = 36)
    private String labSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(nullable = false)
    private Long priority;

    @Column(name = "assigned_to", length = 36)
    private String assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Version
    private Long version; // For optimistic locking

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
        if (priority == null) {
            // Default priority to timestamp epoch for FCFS
            priority = System.currentTimeMillis();
        }
    }

    /**
     * Validate state transition for request status.
     */
    public boolean canTransitionTo(RequestStatus newStatus) {
        return switch (status) {
            case PENDING -> newStatus == RequestStatus.IN_PROGRESS || newStatus == RequestStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == RequestStatus.RESOLVED || newStatus == RequestStatus.PENDING;
            case RESOLVED, CANCELLED -> false;
        };
    }
}