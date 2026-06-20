package com.chitfund.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionServiceTest {

    private final PermissionService permissionService = new PermissionService();

    @Test
    void ownerHasFullAccess() {
        var authentication = auth("OWNER");

        for (Permission permission : Permission.values()) {
            assertThat(permissionService.hasPermission(authentication, permission)).isTrue();
        }
    }

    @Test
    void collectorCanRecordPaymentsButCannotManageUsers() {
        var authentication = auth("COLLECTOR");

        assertThat(permissionService.hasPermission(authentication, Permission.RECORD_PAYMENTS)).isTrue();
        assertThat(permissionService.hasPermission(authentication, Permission.MANAGE_USERS)).isFalse();
    }

    @Test
    void accountantCanViewAuditLogsAndExportReports() {
        var authentication = auth("ACCOUNTANT");

        assertThat(permissionService.hasPermission(authentication, Permission.VIEW_AUDIT_LOGS)).isTrue();
        assertThat(permissionService.hasPermission(authentication, Permission.EXPORT_REPORTS)).isTrue();
        assertThat(permissionService.hasPermission(authentication, Permission.RECORD_PAYMENTS)).isFalse();
    }

    private UsernamePasswordAuthenticationToken auth(String role) {
        return new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
