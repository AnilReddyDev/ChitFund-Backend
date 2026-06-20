package com.chitfund.security;

import com.chitfund.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new ObjectMapper(), "TestSecret_AtLeast32Characters_2026");

    @Test
    void tokenContainsRoleClaimAndParsesAuthenticatedUser() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generate(userId, "admin@company.com", Role.OWNER, Duration.ofMinutes(10));

        assertThat(jwtService.parse(token))
                .hasValue(new AuthenticatedUser(userId, "admin@company.com", Role.OWNER));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generate(UUID.randomUUID(), "admin@company.com", Role.OWNER, Duration.ofMinutes(10));

        assertThat(jwtService.parse(token + "x")).isEmpty();
    }
}
