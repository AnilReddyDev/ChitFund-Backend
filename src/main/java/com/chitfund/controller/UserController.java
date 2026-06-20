package com.chitfund.controller;

import com.chitfund.dto.user.UserCreateRequest;
import com.chitfund.dto.user.UserResponse;
import com.chitfund.dto.user.UserStatusRequest;
import com.chitfund.dto.user.UserUpdateRequest;
import com.chitfund.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_USERS)")
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_USERS)")
    public UserResponse get(@PathVariable UUID id) {
        return userService.get(id);
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_USERS)")
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_USERS)")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_USERS)")
    public UserResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UserStatusRequest request) {
        return userService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(authentication, T(com.chitfund.security.Permission).MANAGE_USERS)")
    public void delete(@PathVariable UUID id) {
        userService.softDelete(id);
    }
}
