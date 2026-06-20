package com.chitfund.audit;

import com.chitfund.service.AuditService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public AuditAspect(AuditService auditService, ObjectMapper objectMapper, EntityManager entityManager) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object idArg = firstIdentifier(joinPoint.getArgs()).orElse(null);
        Map<String, Object> oldValues = loadOldValues(auditable, idArg).orElse(null);
        Object result = joinPoint.proceed();
        Object entity = result == null ? firstEntityArg(joinPoint.getArgs()).orElse(null) : result;
        String entityId = Optional.ofNullable(readId(entity)).orElse(idArg == null ? null : String.valueOf(idArg));
        Map<String, Object> newValues = toMap(result == null ? entity : result);
        auditService.record(auditable.action(), auditable.entityType(), entityId, oldValues, newValues, currentRequest());
        return result;
    }

    private Optional<Map<String, Object>> loadOldValues(Auditable auditable, Object id) {
        if (id == null || auditable.entityClass().equals(Void.class)) {
            return Optional.empty();
        }
        Object entity = entityManager.find(auditable.entityClass(), id);
        return Optional.ofNullable(toMap(entity));
    }

    private Optional<Object> firstIdentifier(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Number || arg instanceof java.util.UUID || arg instanceof String) {
                return Optional.of(arg);
            }
        }
        return Optional.empty();
    }

    private Optional<Object> firstEntityArg(Object[] args) {
        for (Object arg : args) {
            if (arg != null && !(arg instanceof Number) && !(arg instanceof String) && !(arg instanceof java.util.UUID)) {
                return Optional.of(arg);
            }
        }
        return Optional.empty();
    }

    private Map<String, Object> toMap(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return Map.of("value", value);
        }
        if (value instanceof Collection<?> collection) {
            return Map.of("size", collection.size());
        }
        return objectMapper.convertValue(value, new TypeReference<>() {
        });
    }

    private String readId(Object value) {
        if (value == null) {
            return null;
        }
        try {
            Method method = value.getClass().getMethod("getId");
            Object id = method.invoke(value);
            return id == null ? null : String.valueOf(id);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
