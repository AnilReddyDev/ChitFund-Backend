package com.chitfund.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAnnotationTest {

    @Test
    void controllersUseCentralPermissionServiceInsteadOfHardcodedRoles() {
        List<Class<?>> controllers = List.of(
                AuditLogController.class,
                AuctionController.class,
                DashboardController.class,
                GroupController.class,
                GroupMemberController.class,
                LedgerController.class,
                MemberController.class,
                PaymentController.class,
                UserController.class
        );

        List<String> expressions = controllers.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods()))
                .map(this::preAuthorize)
                .filter(expression -> expression != null && !expression.isBlank())
                .toList();

        assertThat(expressions).isNotEmpty();
        assertThat(expressions).allMatch(expression -> expression.contains("@permissionService"));
        assertThat(expressions).noneMatch(expression -> expression.contains("hasRole") || expression.contains("hasAnyRole"));
    }

    private String preAuthorize(Method method) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        return annotation == null ? null : annotation.value();
    }
}
