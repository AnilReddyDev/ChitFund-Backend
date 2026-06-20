package com.chitfund.security;

import com.chitfund.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AppUser {
    private UUID id;
    private String username;
    private String passwordHash;
    private Role role;
    private boolean active;
}
