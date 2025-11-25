package com.lms.repository;

import com.lms.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Reply entity operations.
 */
@Repository
public interface ReplyRepository extends JpaRepository<Reply, String> {

    Page<Reply> findByRequestIdOrderByCreatedAtDesc(String requestId, Pageable pageable);

    List<Reply> findByRequestIdOrderByCreatedAtAsc(String requestId);

    long countByRequestId(String requestId);
}


