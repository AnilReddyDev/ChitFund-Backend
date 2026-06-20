package com.chitfund.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class TokenSession {
    private String token;
    private String username;
    private String role;
    private Instant expiresAt;
}
