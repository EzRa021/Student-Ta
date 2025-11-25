package com.lms.service;

import com.lms.entity.Request;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.RequestRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Service for verifying resource-level authorization.
 * Implements fine-grained access control beyond simple role-based checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    /**
     * ✅ Check if current user is the student who created the request.
     */
    public boolean isRequestCreator(String requestId, String username) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return request.getStudentId().equals(student.getId());
    }

    /**
     * ✅ Check if current user is the TA assigned to the request.
     */
    public boolean isAssignedTA(String requestId, String username) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        User ta = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return request.getAssignedTo() != null &&
                request.getAssignedTo().equals(ta.getId());
    }

    /**
     * ✅ Verify user can resolve request (assigned TA only).
     */
    public void verifyCanResolveRequest(String requestId, String username) {
        if (!isAssignedTA(requestId, username)) {
            throw new AccessDeniedException(
                    "You are not authorized to resolve this request. Only assigned TA can resolve.");
        }
    }

    /**
     * ✅ Verify user can update request priority (assigned TA only).
     */
    public void verifyCanUpdatePriority(String requestId, String username) {
        if (!isAssignedTA(requestId, username)) {
            throw new AccessDeniedException(
                    "You are not authorized to update this request priority. Only assigned TA can update.");
        }
    }

    /**
     * ✅ Verify user can assign themselves to request (TA role only).
     */
    public void verifyCanAssignRequest(String requestId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isTA = user.getRoles().stream()
                .anyMatch(role -> role.getRole() == Role.TA);

        if (!isTA) {
            throw new AccessDeniedException("Only TAs can assign requests to themselves.");
        }
    }

    /**
     * ✅ Verify user can view request (creator or assigned TA).
     */
    public void verifyCanViewRequest(String requestId, String username) {
        if (!isRequestCreator(requestId, username) &&
                !isAssignedTA(requestId, username)) {
            throw new AccessDeniedException(
                    "You are not authorized to view this request.");
        }
    }

    /**
     * ✅ Verify user can update request (creator only).
     */
    public void verifyCanUpdateRequest(String requestId, String username) {
        if (!isRequestCreator(requestId, username)) {
            throw new AccessDeniedException(
                    "You are not authorized to update this request. Only creator can update.");
        }
    }

    /**
     * ✅ Verify user can delete request (creator only).
     */
    public void verifyCanDeleteRequest(String requestId, String username) {
        if (!isRequestCreator(requestId, username)) {
            throw new AccessDeniedException(
                    "You are not authorized to delete this request. Only creator can delete.");
        }
    }
}
