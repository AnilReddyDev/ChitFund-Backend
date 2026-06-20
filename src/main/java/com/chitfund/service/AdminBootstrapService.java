package com.chitfund.service;

import com.chitfund.entity.AppUserEntity;
import com.chitfund.entity.Role;
import com.chitfund.repository.AppUserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class AdminBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${CHITFUND_INITIAL_ADMIN_USERNAME:admin}")
    private String initialAdminUsername;

    @Value("${CHITFUND_INITIAL_ADMIN_PASSWORD:}")
    private String initialAdminPassword;

    public AdminBootstrapService(AppUserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @PostConstruct
    @Transactional
    public void createInitialAdminIfNeeded() {
        if (userRepository.count() > 0) {
            return;
        }

        if (initialAdminUsername == null || initialAdminUsername.isBlank()) {
            throw new IllegalStateException("Initial admin username must not be blank.");
        }

        String password = initialAdminPassword.isBlank() ? generateTemporaryPassword() : initialAdminPassword;
        requireStrongPassword(password);

        AppUserEntity admin = new AppUserEntity();
        admin.setId(UUID.randomUUID());
        admin.setUsername(initialAdminUsername);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.OWNER);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());

        AppUserEntity saved = userRepository.save(admin);
        authService.cacheUser(saved);

        if (initialAdminPassword.isBlank()) {
            log.warn("Created initial admin user '{}'. Temporary password: {}", initialAdminUsername, password);
        } else {
            log.info("Created initial admin user '{}' from CHITFUND_INITIAL_ADMIN_PASSWORD.", initialAdminUsername);
        }
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[18];
        secureRandom.nextBytes(bytes);
        return "Admin#" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void requireStrongPassword(String password) {
        if (password == null || password.length() < 12
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalStateException("Initial admin password must be at least 12 characters and include upper, lower, number, and symbol.");
        }
    }
}
