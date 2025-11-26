package com.lms.ui.model;

/**
 * Enumeration for request status.
 */
public enum RequestStatus {
    PENDING("Pending", "#FFC107"),
    IN_PROGRESS("In Progress", "#2196F3"),
    RESOLVED("Resolved", "#4CAF50"),
    CANCELLED("Cancelled", "#F44336");

    private final String displayName;
    private final String colorHex;

    RequestStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    @Override
    public String toString() {
        return displayName;
    }
}