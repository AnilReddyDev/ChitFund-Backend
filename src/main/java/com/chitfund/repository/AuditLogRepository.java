package com.chitfund.repository;

import com.chitfund.entity.AuditLog;
import com.chitfund.entity.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
    boolean existsByActionAndEntityTypeAndEntityIdAndPerformedByAndCreatedAtAfter(
            AuditAction action,
            String entityType,
            String entityId,
            UUID performedBy,
            LocalDateTime createdAt
    );
}
