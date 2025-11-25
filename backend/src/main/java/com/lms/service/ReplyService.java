package com.lms.service;

import com.lms.dto.ReplyDto;
import com.lms.entity.Reply;
import com.lms.entity.Request;
import com.lms.entity.RequestStatus;
import com.lms.entity.User;
import com.lms.repository.ReplyRepository;
import com.lms.repository.RequestRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing replies to requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    /**
     * Create a reply to a request (TA only).
     * Validates that the TA can reply to this request.
     */
    @Transactional
    public ReplyDto createReply(String requestId, String message, String taUsername) {
        // Validate inputs
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Reply message cannot be empty");
        }

        User ta = userRepository.findByUsername(taUsername)
                .orElseThrow(() -> new IllegalArgumentException("TA not found: " + taUsername));

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        // Validate request status - cannot reply to cancelled requests
        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reply to cancelled requests");
        }

        // Only the assigned TA can reply (or any TA if unassigned)
        if (request.getAssignedTo() != null && !request.getAssignedTo().equals(ta.getId())) {
            throw new IllegalStateException("You can only reply to requests assigned to you");
        }

        Reply reply = Reply.builder()
                .requestId(requestId)
                .taId(ta.getId())
                .message(message.trim())
                .build();

        reply = replyRepository.save(reply);
        log.info("Reply created for request {} by TA: {}", requestId, taUsername);

        return mapToDto(reply, ta.getUsername());
    }

    /**
     * Get all replies for a request.
     * Returns replies in chronological order (oldest first).
     */
    @Transactional(readOnly = true)
    public List<ReplyDto> getRepliesByRequestId(String requestId) {
        // Verify request exists
        if (!requestRepository.existsById(requestId)) {
            throw new IllegalArgumentException("Request not found: " + requestId);
        }

        List<Reply> replies = replyRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
        return replies.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get replies with pagination.
     * Returns most recent replies first when paginated.
     */
    @Transactional(readOnly = true)
    public Page<ReplyDto> getRepliesByRequestId(String requestId, int page, int size) {
        // Verify request exists
        if (!requestRepository.existsById(requestId)) {
            throw new IllegalArgumentException("Request not found: " + requestId);
        }

        // Validate pagination parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Reply> replies = replyRepository.findByRequestIdOrderByCreatedAtDesc(requestId, pageable);
        return replies.map(this::mapToDto);
    }

    /**
     * Map Reply entity to ReplyDto.
     * Safely handles missing TA information.
     */
    private ReplyDto mapToDto(Reply reply) {
        String taUsername = userRepository.findById(reply.getTaId())
                .map(User::getUsername)
                .orElse("Unknown TA");

        return mapToDto(reply, taUsername);
    }

    /**
     * Map Reply entity to ReplyDto with known TA username.
     */
    private ReplyDto mapToDto(Reply reply, String taUsername) {
        return ReplyDto.builder()
                .id(reply.getId())
                .requestId(reply.getRequestId())
                .taId(reply.getTaId())
                .taUsername(taUsername)
                .message(reply.getMessage())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}