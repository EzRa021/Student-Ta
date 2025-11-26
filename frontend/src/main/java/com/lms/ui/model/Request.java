package com.lms.ui.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Request model representing a student help request.
 */
public class Request {
    private String id;
    private String title;
    private String description;
    private String studentId;
    private String studentUsername;
    private RequestStatus status;
    private Long priority;
    private String assignedTo;
    private String assignedToUsername;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // Constructors
    public Request() {
    }

    public Request(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = RequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedToUsername() {
        return assignedToUsername;
    }

    public void setAssignedToUsername(String assignedToUsername) {
        this.assignedToUsername = assignedToUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    /**
     * Get formatted creation time.
     */
    public String getFormattedCreatedAt() {
        if (createdAt == null)
            return "";
        return createdAt.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * Get full formatted creation timestamp (date and time).
     */
    public String getFormattedCreatedAtFull() {
        if (createdAt == null)
            return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
    }

    /**
     * Get full formatted resolved timestamp (date and time).
     */
    public String getFormattedResolvedAtFull() {
        if (resolvedAt == null)
            return "";
        return resolvedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
    }

    /**
     * Get wait time in seconds if request is still pending.
     */
    public long getWaitTimeSeconds() {
        if (createdAt == null)
            return 0;
        return java.time.temporal.ChronoUnit.SECONDS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Get priority level as text (High, Medium, Low).
     */
    public String getPriorityText() {
        if (priority == null || priority == 0)
            return "Low";
        if (priority <= 2)
            return "Low";
        if (priority <= 5)
            return "Medium";
        return "High";
    }

    /**
     * Get priority color style for UI.
     */
    public String getPriorityStyle() {
        String priority = getPriorityText();
        switch (priority) {
            case "High":
                return "-fx-background-color: #e74c3c; -fx-text-fill: white;";
            case "Medium":
                return "-fx-background-color: #f39c12; -fx-text-fill: white;";
            case "Low":
                return "-fx-background-color: #3498db; -fx-text-fill: white;";
            default:
                return "-fx-background-color: #95a5a6; -fx-text-fill: white;";
        }
    }

    @Override
    public String toString() {
        return title + " - " + status;
    }
}