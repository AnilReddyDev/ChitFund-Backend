package com.chitfund.service;

import com.chitfund.audit.Auditable;
import com.chitfund.dto.user.UserCreateRequest;
import com.chitfund.dto.user.UserResponse;
import com.chitfund.dto.user.UserStatusRequest;
import com.chitfund.dto.user.UserUpdateRequest;
import com.chitfund.entity.AppUserEntity;
import com.chitfund.entity.AuditAction;
import com.chitfund.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final AuditService auditService;

    public UserService(AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthService authService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.auditService = auditService;
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    @Auditable(action = AuditAction.CREATE, entityType = "User", entityClass = AppUserEntity.class)
    public UserResponse create(UserCreateRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            throw new IllegalArgumentException("Username already exists");
        });
        AppUserEntity user = new AppUserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        auditService.currentUser().ifPresent(actor -> user.setCreatedBy(actor.id()));
        AppUserEntity saved = userRepository.save(user);
        authService.cacheUser(saved);
        return toResponse(saved);
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityType = "User", entityClass = AppUserEntity.class)
    public UserResponse update(UUID id, UserUpdateRequest request) {
        AppUserEntity user = load(id);
        user.setRole(request.getRole());
        auditService.currentUser().ifPresent(actor -> user.setUpdatedBy(actor.id()));
        AppUserEntity saved = userRepository.save(user);
        authService.cacheUser(saved);
        return toResponse(saved);
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityType = "User", entityClass = AppUserEntity.class)
    public UserResponse updateStatus(UUID id, UserStatusRequest request) {
        AppUserEntity user = load(id);
        user.setActive(request.getActive());
        user.setUpdatedAt(LocalDateTime.now());
        auditService.currentUser().ifPresent(actor -> user.setUpdatedBy(actor.id()));
        AppUserEntity saved = userRepository.save(user);
        if (Boolean.TRUE.equals(saved.getActive())) {
            authService.cacheUser(saved);
        } else {
            authService.evictUser(saved.getUsername());
        }
        return toResponse(saved);
    }

    @Transactional
    @Auditable(action = AuditAction.DELETE, entityType = "User", entityClass = AppUserEntity.class)
    public void softDelete(UUID id) {
        AppUserEntity user = load(id);
        user.setActive(false);
        auditService.currentUser().ifPresent(actor -> user.setUpdatedBy(actor.id()));
        AppUserEntity saved = userRepository.save(user);
        authService.evictUser(saved.getUsername());
    }

    private AppUserEntity load(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UserResponse toResponse(AppUserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getActive(),
                user.getLastLogin(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
