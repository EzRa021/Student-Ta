package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User entity representing students, TAs, and admins in the system.
 * Passwords are stored as BCrypt hashes.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "roles")
@EqualsAndHashCode(exclude = "roles")
public class User {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<UserRole> roles = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (roles == null) {
            roles = new HashSet<>();
        }
    }

    /**
     * Helper method to add a role to this user.
     */
    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        UserRole userRole = new UserRole();
        userRole.setUser(this);
        userRole.setRole(role);
        roles.add(userRole);
    }

    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(Role role) {
        return roles.stream().anyMatch(ur -> ur.getRole() == role);
    }
}