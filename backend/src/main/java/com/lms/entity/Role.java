package com.lms.entity;

/**
 * Role enumeration for RBAC.
 * STUDENT - can create and view own requests
 * TA - can view all requests, assign and resolve them
 */
public enum Role {
    STUDENT,
    TA
}