package com.lms.ui.model;

import java.time.LocalDateTime;

/**
 * DTO for reply data.
 */
public class ReplyDto {
    private String id;
    private String requestId;
    private String taId;
    private String taUsername;
    private String message;
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTaId() {
        return taId;
    }

    public void setTaId(String taId) {
        this.taId = taId;
    }

    public String getTaUsername() {
        return taUsername;
    }

    public void setTaUsername(String taUsername) {
        this.taUsername = taUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null)
            return "";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd HH:mm"));
    }
}
