package com.chitfund.service;

import com.chitfund.entity.AuditAction;
import com.chitfund.entity.AuditLog;
import com.chitfund.repository.AuditLogRepository;
import com.chitfund.security.AuthenticatedUser;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String action, String entityType, Long entityId, String details) {
        record(AuditAction.UPDATE, entityType, String.valueOf(entityId), Map.of("details", details), null, null);
    }

    public AuditLog record(AuditAction action,
                           String entityType,
                           String entityId,
                           Map<String, Object> oldValues,
                           Map<String, Object> newValues,
                           HttpServletRequest request) {
        AuthenticatedUser actor = currentUser().orElse(null);
        AuditLog auditLog = new AuditLog();
        auditLog.setId(UUID.randomUUID());
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setPerformedBy(actor == null ? null : actor.id());
        auditLog.setPerformedByName(actor == null ? currentActor() : actor.username());
        auditLog.setPerformedByRole(actor == null ? null : actor.role().name());
        auditLog.setIpAddress(request == null ? null : clientIp(request));
        auditLog.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
        return auditLog;
    }

    public Page<AuditLog> search(String entityType,
                                 String entityId,
                                 AuditAction action,
                                 UUID performedBy,
                                 LocalDateTime startDate,
                                 LocalDateTime endDate,
                                 Pageable pageable) {
        return auditLogRepository.findAll((root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (entityId != null && !entityId.isBlank()) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (performedBy != null) {
                predicates.add(cb.equal(root.get("performedBy"), performedBy));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }

    public AuditLog get(UUID id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Audit log not found"));
    }

    public Optional<AuthenticatedUser> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        return authentication.getName();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
