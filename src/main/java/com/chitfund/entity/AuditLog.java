package com.chitfund.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private UUID id;

    @Column(length = 100, updatable = false)
    private String entityType;

    @Column(length = 100, updatable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, updatable = false)
    private AuditAction action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> oldValues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> newValues;

    @Column(updatable = false)
    private UUID performedBy;

    @Column(updatable = false)
    private String performedByName;

    @Column(length = 50, updatable = false)
    private String performedByRole;

    @Column(length = 100, updatable = false)
    private String ipAddress;

    @Column(columnDefinition = "text", updatable = false)
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PreUpdate
    @PreRemove
    void preventMutation() {
        throw new UnsupportedOperationException("Audit logs are immutable");
    }
}
