package com.chitfund.service;

import com.chitfund.entity.AuditAction;
import com.chitfund.entity.AuditLog;
import com.chitfund.entity.Role;
import com.chitfund.repository.AuditLogRepository;
import com.chitfund.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditServiceTest {

    private final AuditLogRepository repository = mock(AuditLogRepository.class);
    private final AuditService auditService = new AuditService(repository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recordCapturesAuthenticatedUserAndRequestMetadata() {
        UUID userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userId, "owner@example.com", Role.OWNER),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_OWNER"))
        ));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        auditService.record(
                AuditAction.CREATE,
                "Member",
                "42",
                null,
                Map.of("name", "Anil"),
                request
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPerformedBy()).isEqualTo(userId);
        assertThat(saved.getPerformedByName()).isEqualTo("owner@example.com");
        assertThat(saved.getPerformedByRole()).isEqualTo("OWNER");
        assertThat(saved.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(saved.getUserAgent()).isEqualTo("JUnit");
        assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
    }
}
