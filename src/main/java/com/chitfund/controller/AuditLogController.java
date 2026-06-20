package com.chitfund.controller;

import com.chitfund.entity.AuditLog;
import com.chitfund.entity.AuditAction;
import com.chitfund.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).VIEW_AUDIT_LOGS)")
    public Page<AuditLog> getAll(@RequestParam(required = false) String entityType,
                                 @RequestParam(required = false) String entityId,
                                 @RequestParam(required = false) AuditAction action,
                                 @RequestParam(required = false) UUID performedBy,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                 Pageable pageable) {
        return auditService.search(entityType, entityId, action, performedBy, startDate, endDate, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).VIEW_AUDIT_LOGS)")
    public AuditLog get(@PathVariable UUID id) {
        return auditService.get(id);
    }
}
