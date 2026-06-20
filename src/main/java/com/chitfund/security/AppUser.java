package com.chitfund.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppUser {
    private String username;
    private String passwordHash;
    private String role;
}
