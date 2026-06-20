package com.chitfund.security;

import com.chitfund.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TokenSession {
    private String token;
    private UUID userId;
    private String username;
    private Role role;
    private Instant expiresAt;
}
