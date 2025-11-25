package com.lms.controller;

import com.lms.dto.CreateRequestDto;
import com.lms.dto.RequestResponse;
import com.lms.dto.UpdateRequestDto;
import com.lms.entity.RequestStatus;
import com.lms.service.AuthorizationService;  // ✅ Import
import com.lms.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for request management endpoints.
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestService requestService;
    private final AuthorizationService authorizationService;  // ✅ Inject service

    /**
     * Create a new help request (Student only).
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RequestResponse> createRequest(
            @Valid @RequestBody CreateRequestDto dto,
            Authentication authentication) {
        log.info("Create request by: {}", authentication.getName());
        RequestResponse response = requestService.createRequest(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all requests with optional filtering (TA only).
     */
    @GetMapping
    @PreAuthorize("hasRole('TA')")
    public ResponseEntity<Page<RequestResponse>> getAllRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "priority") String sort) {
        log.info("Get all requests - Status: {}, Page: {}, Size: {}", status, page, size);
        Page<RequestResponse> requests = requestService.getAllRequests(status, page, size, sort);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get student's own requests (Student only).
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<RequestResponse>> getMyRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        log.info("Get my requests for user: {}", authentication.getName());
        Page<RequestResponse> requests = requestService.getMyRequests(
                authentication.getName(), status, page, size);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get a single request by ID (Student can view their own, TA can view assigned).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TA')")
    public ResponseEntity<RequestResponse> getRequestById(
            @PathVariable String id,
            Authentication authentication) {
        log.info("Get request by ID: {}", id);
        
        // ✅ SECURITY FIX: Verify user is authorized to view this request
        authorizationService.verifyCanViewRequest(id, authentication.getName());
        
        RequestResponse response = requestService.getRequestById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Assign a request to TA (TA only).
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('TA')")
    public ResponseEntity<RequestResponse> assignRequest(
            @PathVariable String id,
            Authentication authentication) {
        log.info("Assign request {} to TA: {}", id, authentication.getName());
        
        // ✅ SECURITY FIX: Verify user is a TA
        authorizationService.verifyCanAssignRequest(id, authentication.getName());
        
        RequestResponse response = requestService.assignRequest(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Mark request as resolved (only assigned TA or admin can resolve).
     */
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('TA')")
    public ResponseEntity<RequestResponse> resolveRequest(
            @PathVariable String id,
            Authentication authentication) {
        log.info("Resolve request {} by TA: {}", id, authentication.getName());
        
        // ✅ SECURITY FIX: Verify user is assigned TA or admin
        authorizationService.verifyCanResolveRequest(id, authentication.getName());
        
        RequestResponse response = requestService.resolveRequest(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Update request priority (only assigned TA or admin can update).
     */
    @PutMapping("/{id}/priority")
    @PreAuthorize("hasRole('TA')")
    public ResponseEntity<RequestResponse> updatePriority(
            @PathVariable String id,
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long priority = body.get("priority");
        if (priority == null) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Update priority for request {} to: {}", id, priority);
        
        // ✅ SECURITY FIX: Verify user is assigned TA or admin
        authorizationService.verifyCanUpdatePriority(id, authentication.getName());

        RequestResponse response = requestService.updatePriority(id, priority);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a request (only creator or admin can update).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RequestResponse> updateRequest(
            @PathVariable String id,
            @Valid @RequestBody UpdateRequestDto dto,
            Authentication authentication) {
        log.info("Update request {} by: {}", id, authentication.getName());
        
        // ✅ SECURITY FIX: Verify user is creator or admin
        authorizationService.verifyCanUpdateRequest(id, authentication.getName());
        
        RequestResponse response = requestService.updateRequest(id, dto, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a request (only creator or admin can delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> deleteRequest(
            @PathVariable String id,
            Authentication authentication) {
        log.info("Delete request {} by: {}", id, authentication.getName());
        
        // ✅ SECURITY FIX: Verify user is creator or admin
        authorizationService.verifyCanDeleteRequest(id, authentication.getName());
        
        requestService.deleteRequest(id, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Request deleted successfully"));
    }
}