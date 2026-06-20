package com.chitfund.security;

import com.chitfund.entity.Role;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class PermissionService {

    private static final Map<Role, Set<Permission>> PERMISSIONS = new EnumMap<>(Role.class);

    static {
        PERMISSIONS.put(Role.OWNER, EnumSet.allOf(Permission.class));
        PERMISSIONS.put(Role.MANAGER, EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.MANAGE_GROUPS,
                Permission.VIEW_GROUPS,
                Permission.MANAGE_MEMBERS,
                Permission.VIEW_MEMBERS,
                Permission.RECORD_PAYMENTS,
                Permission.VIEW_PAYMENTS,
                Permission.MANAGE_AUCTIONS,
                Permission.VIEW_AUCTIONS,
                Permission.VIEW_AUDIT_LOGS,
                Permission.VIEW_REPORTS,
                Permission.EXPORT_REPORTS
        ));
        PERMISSIONS.put(Role.COLLECTOR, EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.VIEW_MEMBERS,
                Permission.RECORD_PAYMENTS,
                Permission.VIEW_PAYMENTS
        ));
        PERMISSIONS.put(Role.ACCOUNTANT, EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.VIEW_REPORTS,
                Permission.EXPORT_REPORTS,
                Permission.VIEW_AUDIT_LOGS
        ));
        PERMISSIONS.put(Role.VIEWER, EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.VIEW_GROUPS,
                Permission.VIEW_MEMBERS,
                Permission.VIEW_PAYMENTS,
                Permission.VIEW_AUCTIONS,
                Permission.VIEW_REPORTS
        ));
    }

    public boolean hasPermission(Authentication authentication, Permission permission) {
        if (authentication == null || !authentication.isAuthenticated() || permission == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .map(this::parseRole)
                .anyMatch(role -> PERMISSIONS.getOrDefault(role, Set.of()).contains(permission));
    }

    private Role parseRole(String value) {
        try {
            return Role.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return Role.VIEWER;
        }
    }
}
