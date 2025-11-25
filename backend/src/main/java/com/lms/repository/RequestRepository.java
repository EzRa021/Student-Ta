package com.lms.repository;

import com.lms.entity.Request;
import com.lms.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Request entity operations.
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, String> {

    Page<Request> findByStatus(RequestStatus status, Pageable pageable);

    Page<Request> findByStudentId(String studentId, Pageable pageable);

    Page<Request> findByStudentIdAndStatus(String studentId, RequestStatus status, Pageable pageable);

    @Query("SELECT r FROM Request r WHERE r.status = :status ORDER BY r.priority ASC, r.createdAt ASC")
    List<Request> findByStatusOrderByPriorityAndCreatedAt(@Param("status") RequestStatus status);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.status = :status")
    long countByStatus(@Param("status") RequestStatus status);

    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, r.createdAt, r.resolvedAt)) FROM Request r WHERE r.status = :status AND r.resolvedAt IS NOT NULL")
    Double getAverageWaitTimeByStatus(@Param("status") RequestStatus status);

    List<Request> findByStatusAndCreatedAtBefore(RequestStatus status, LocalDateTime dateTime);

    long countByAssignedTo(String taId);

    long countByAssignedToAndStatus(String taId, RequestStatus status);

    List<Request> findByStudentIdOrderByCreatedAtDesc(String studentId);

    List<Request> findByAssignedToOrderByCreatedAtDesc(String taId);

    List<Request> findByAssignedToAndStatusOrderByCreatedAtDesc(String taId, RequestStatus status);

    @Query("SELECT r FROM Request r WHERE r.assignedTo = :taId AND r.status = :status ORDER BY r.createdAt ASC")
    List<Request> findByAssignedToAndStatusOrderByCreatedAtAsc(@Param("taId") String taId,
            @Param("status") RequestStatus status);

    @Query("SELECT r FROM Request r WHERE r.status = :status ORDER BY r.createdAt ASC")
    Page<Request> findByStatusOrderByCreatedAtAsc(@Param("status") RequestStatus status, Pageable pageable);
}