package com.lms.ui.model;

/**
 * Enumeration for user roles in the system.
 */
public enum UserRole {
    STUDENT("Student"),
    TA("Teaching Assistant");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}