package com.chitfund.service;

import com.chitfund.entity.AuditLog;
import com.chitfund.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String action, String entityType, Long entityId, String details) {
        AuditLog auditLog = new AuditLog(
                null,
                currentActor(),
                action,
                entityType,
                entityId,
                details,
                LocalDateTime.now()
        );
        auditLogRepository.save(auditLog);
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        return authentication.getName();
    }
}
