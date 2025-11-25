package com.lms.controller;

import com.lms.dto.ReplyCreateDto;  // ✅ Import
import com.lms.dto.ReplyDto;
import com.lms.service.ReplyService;
import jakarta.validation.Valid;  // ✅ Import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for reply management endpoints.
 */
@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
@Slf4j
public class ReplyController {

    private final ReplyService replyService;

    /**
     * Create a reply to a request (TA only).
     */
    @PostMapping("/request/{requestId}")
    @PreAuthorize("hasRole('TA')")
    public ResponseEntity<ReplyDto> createReply(
            @PathVariable String requestId,
            @Valid @RequestBody ReplyCreateDto dto,  // ✅ Use ReplyCreateDto with validation
            Authentication authentication) {

        log.info("Create reply for request {} by TA: {}", requestId, authentication.getName());
        ReplyDto reply = replyService.createReply(requestId, dto.getMessage().trim(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    /**
     * Get all replies for a request (Students and TAs can view).
     */
    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TA')")
    public ResponseEntity<List<ReplyDto>> getRepliesByRequestId(@PathVariable String requestId) {
        log.info("Get replies for request: {}", requestId);
        List<ReplyDto> replies = replyService.getRepliesByRequestId(requestId);
        return ResponseEntity.ok(replies);
    }

    /**
     * Get replies for a request with pagination (Students and TAs can view).
     */
    @GetMapping("/request/{requestId}/page")
    @PreAuthorize("hasAnyRole('STUDENT', 'TA')")
    public ResponseEntity<Page<ReplyDto>> getRepliesByRequestId(
            @PathVariable String requestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Get replies for request {} - Page: {}, Size: {}", requestId, page, size);
        Page<ReplyDto> replies = replyService.getRepliesByRequestId(requestId, page, size);
        return ResponseEntity.ok(replies);
    }
}
