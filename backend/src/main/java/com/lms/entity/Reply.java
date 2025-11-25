package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reply entity representing TA responses to student requests.
 */
@Entity
@Table(name = "replies", indexes = {
    @Index(name = "idx_request_id", columnList = "request_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "request_id", nullable = false, length = 36)
    private String requestId;

    @Column(name = "ta_id", nullable = false, length = 36)
    private String taId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}


