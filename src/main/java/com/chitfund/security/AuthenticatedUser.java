package com.chitfund.security;

import com.chitfund.entity.Role;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String username, Role role) {
}
