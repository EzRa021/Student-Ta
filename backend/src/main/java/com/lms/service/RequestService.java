package com.lms.service;

import com.lms.dto.CreateRequestDto;
import com.lms.dto.RequestResponse;
import com.lms.dto.UpdateRequestDto;
import com.lms.dto.WebSocketEvent;
import com.lms.entity.Request;
import com.lms.entity.RequestStatus;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.repository.RequestRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing help requests.
 * Implements FCFS prioritization with manual override capability.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Create a new help request for a student.
     * Automatically sets priority based on timestamp for FCFS ordering.
     */
    @Transactional
    public RequestResponse createRequest(CreateRequestDto dto, String username) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Request request = Request.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .studentId(student.getId())
                .labSessionId(dto.getLabSessionId())
                .status(RequestStatus.PENDING)
                .build();

        request = requestRepository.save(request);
        log.info("Request created: {} by user: {}", request.getId(), username);

        RequestResponse response = mapToResponse(request);

        // Broadcast WebSocket event
        broadcastEvent("request:created", response);

        return response;
    }

    /**
     * Get all requests with optional filtering and pagination.
     */
    @Transactional(readOnly = true)
    public Page<RequestResponse> getAllRequests(RequestStatus status, int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.ASC, "priority").and(Sort.by(Sort.Direction.ASC, "createdAt"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Request> requests;
        if (status != null) {
            requests = requestRepository.findByStatus(status, pageable);
        } else {
            requests = requestRepository.findAll(pageable);
        }

        return requests.map(this::mapToResponse);
    }

    /**
     * Get requests for a specific student.
     */
    @Transactional(readOnly = true)
    public Page<RequestResponse> getMyRequests(String username, RequestStatus status, int page, int size) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Request> requests;
        if (status != null) {
            requests = requestRepository.findByStudentIdAndStatus(student.getId(), status, pageable);
        } else {
            requests = requestRepository.findByStudentId(student.getId(), pageable);
        }

        return requests.map(this::mapToResponse);
    }

    /**
     * Get a single request by ID.
     */
    @Transactional(readOnly = true)
    public RequestResponse getRequestById(String id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        return mapToResponse(request);
    }

    /**
     * Assign a request to a TA (claim).
     * Uses optimistic locking to prevent concurrent claims.
     */
    @Transactional
    public RequestResponse assignRequest(String requestId, String taUsername) {
        User ta = userRepository.findByUsername(taUsername)
                .orElseThrow(() -> new IllegalArgumentException("TA not found"));

        try {
            Request request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Request not found"));

            // Validate status transition
            if (!request.canTransitionTo(RequestStatus.IN_PROGRESS)) {
                throw new IllegalStateException("Cannot assign request in current status: " + request.getStatus());
            }

            // Check if already assigned
            if (request.getAssignedTo() != null) {
                throw new IllegalStateException("Request already assigned");
            }

            request.setStatus(RequestStatus.IN_PROGRESS);
            request.setAssignedTo(ta.getId());

            request = requestRepository.save(request);
            log.info("Request {} assigned to TA: {}", requestId, taUsername);

            RequestResponse response = mapToResponse(request);

            // Broadcast WebSocket event
            broadcastEvent("request:assigned", response);

            return response;
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure for request {}: {}", requestId, e.getMessage());
            throw new IllegalStateException("Request was already claimed by another TA");
        }
    }

    /**
     * Mark a request as resolved.
     */
    @Transactional
    public RequestResponse resolveRequest(String requestId, String taUsername) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Validate status transition
        if (!request.canTransitionTo(RequestStatus.RESOLVED)) {
            throw new IllegalStateException("Cannot resolve request in current status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.RESOLVED);
        request.setResolvedAt(LocalDateTime.now());

        request = requestRepository.save(request);
        log.info("Request {} resolved by TA: {}", requestId, taUsername);

        RequestResponse response = mapToResponse(request);

        // Broadcast WebSocket event
        broadcastEvent("request:resolved", response);

        return response;
    }

    /**
     * Update request priority (TA can re-order requests).
     */
    @Transactional
    public RequestResponse updatePriority(String requestId, Long newPriority) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        request.setPriority(newPriority);
        request = requestRepository.save(request);

        log.info("Request {} priority updated to: {}", requestId, newPriority);

        RequestResponse response = mapToResponse(request);

        // Broadcast WebSocket event
        broadcastEvent("request:updated", response);

        return response;
    }

    /**
     * Update a request (student can update their own requests).
     */
    @Transactional
    public RequestResponse updateRequest(String requestId, UpdateRequestDto dto, String username) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user owns the request
        if (!request.getStudentId().equals(user.getId())) {
            throw new IllegalStateException("You can only update your own requests");
        }

        // Students cannot update requests once they are assigned to a TA
        if (request.getAssignedTo() != null) {
            throw new IllegalStateException(
                    "Cannot update request: It has been assigned to a TA. Please contact the TA for changes.");
        }

        // Don't allow updating resolved or cancelled requests
        if (request.getStatus() == RequestStatus.RESOLVED || request.getStatus() == RequestStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a " + request.getStatus() + " request");
        }

        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());

        request = requestRepository.save(request);
        log.info("Request {} updated by user: {}", requestId, username);

        RequestResponse response = mapToResponse(request);

        // Broadcast WebSocket event
        broadcastEvent("request:updated", response);

        return response;
    }

    /**
     * Delete a request (student can delete their own requests).
     */
    @Transactional
    public void deleteRequest(String requestId, String username) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user owns the request
        if (!request.getStudentId().equals(user.getId())) {
            throw new IllegalStateException("You can only delete your own requests");
        }

        // Hard delete
        requestRepository.delete(request);

        log.info("Request {} deleted by user: {}", requestId, username);

        // Broadcast deletion event
        RequestResponse response = mapToResponse(request);
        broadcastEvent("request:deleted", response);
    }

    /**
     * Map Request entity to RequestResponse DTO.
     */
    private RequestResponse mapToResponse(Request request) {
        String studentUsername = userRepository.findById(request.getStudentId())
                .map(User::getUsername).orElse(null);

        String assignedToUsername = null;
        if (request.getAssignedTo() != null) {
            assignedToUsername = userRepository.findById(request.getAssignedTo())
                    .map(User::getUsername).orElse(null);
        }

        return RequestResponse.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .studentId(request.getStudentId())
                .studentUsername(studentUsername)
                .labSessionId(request.getLabSessionId())
                .status(request.getStatus())
                .priority(request.getPriority())
                .assignedTo(request.getAssignedTo())
                .assignedToUsername(assignedToUsername)
                .createdAt(request.getCreatedAt())
                .resolvedAt(request.getResolvedAt())
                .metadata(request.getMetadata())
                .build();
    }

    /**
     * Broadcast WebSocket events to connected clients.
     * Students receive only their own request updates.
     * TAs receive all request updates.
     */
    private void broadcastEvent(String eventType, RequestResponse payload) {
        if (payload == null) {
            log.warn("Attempted to broadcast event {} with null payload", eventType);
            return;
        }

        try {
            WebSocketEvent event = WebSocketEvent.of(eventType, payload);

            // Broadcast to all TAs
            messagingTemplate.convertAndSend("/topic/requests", event);

            // Send to specific student if username is available
            if (payload.getStudentUsername() != null && !payload.getStudentUsername().isEmpty()) {
                messagingTemplate.convertAndSendToUser(
                        payload.getStudentUsername(),
                        "/queue/requests",
                        event);
            }

            log.debug("WebSocket event broadcasted: {} for request: {}", eventType, payload.getId());
        } catch (Exception e) {
            log.error("Error broadcasting WebSocket event {}: {}", eventType, e.getMessage(), e);
        }
    }
}