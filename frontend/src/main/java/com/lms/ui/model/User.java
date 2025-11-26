package com.lms.ui.model;

import java.time.LocalDateTime;

/**
 * User model representing a student or TA in the system.
 */
public class User {
    private String id;
    private String username;
    private String email;
    private String password; // Only used during registration
    private UserRole role;
    private String studentId; // For students only
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long pendingAssignments;
    private Long resolvedAssignments;
    private Long totalAssignments;
    private String sessionId; // Session identifier for tracking user connections

    // Constructors
    public User() {
    }

    public User(String username, String email, UserRole role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getPendingAssignments() {
        return pendingAssignments;
    }

    public void setPendingAssignments(Long pendingAssignments) {
        this.pendingAssignments = pendingAssignments;
    }

    public Long getResolvedAssignments() {
        return resolvedAssignments;
    }

    public void setResolvedAssignments(Long resolvedAssignments) {
        this.resolvedAssignments = resolvedAssignments;
    }

    public Long getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(Long totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null)
            return "N/A";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}