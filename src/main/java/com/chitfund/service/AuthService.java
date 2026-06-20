package com.chitfund.service;

import com.chitfund.entity.AppUserEntity;
import com.chitfund.dto.LoginRequest;
import com.chitfund.dto.LoginResponse;
import com.chitfund.entity.AuditAction;
import com.chitfund.repository.AppUserRepository;
import com.chitfund.security.AppUser;
import com.chitfund.security.JwtService;
import com.chitfund.security.TokenSession;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, AppUser> users = new ConcurrentHashMap<>();
    private final Map<String, TokenSession> sessions = new ConcurrentHashMap<>();
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Value("${app.security.token-ttl-minutes:480}")
    private long tokenTtlMinutes;

    public AuthService(PasswordEncoder passwordEncoder,
                       AppUserRepository userRepository,
                       JwtService jwtService,
                       AuditService auditService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @PostConstruct
    @Transactional
    public void initUsers() {
        userRepository.findAll().forEach(this::cacheUser);
    }

    public LoginResponse login(LoginRequest request) {
        return login(request, null);
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        AppUser user = users.get(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!user.isActive()) {
            throw new BadCredentialsException("User is inactive");
        }

        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(tokenTtlMinutes));
        String token = jwtService.generate(user.getId(), user.getUsername(), user.getRole(), Duration.ofMinutes(tokenTtlMinutes));
        TokenSession session = new TokenSession(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole(),
                expiresAt
        );
        sessions.put(token, session);
        userRepository.findById(user.getId()).ifPresent(entity -> {
            entity.setLastLogin(LocalDateTime.now());
            userRepository.save(entity);
        });
        auditService.record(
                AuditAction.LOGIN,
                "User",
                String.valueOf(user.getId()),
                null,
                Map.of("username", user.getUsername(), "role", user.getRole().name()),
                servletRequest
        );

        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }

    public Optional<TokenSession> findValidSession(String token) {
        if (revokedTokens.contains(token)) {
            return Optional.empty();
        }
        TokenSession session = sessions.get(token);
        if (session == null) {
            Optional<TokenSession> parsed = jwtService.parse(token).map(user -> new TokenSession(
                    token,
                    user.id(),
                    user.username(),
                    user.role(),
                    Instant.now().plus(Duration.ofMinutes(tokenTtlMinutes))
            ));
            parsed.ifPresent(value -> sessions.put(token, value));
            session = parsed.orElse(null);
        }
        if (session == null) {
            return Optional.empty();
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void logout(String token, HttpServletRequest servletRequest) {
        if (token == null || token.isBlank()) {
            return;
        }
        findValidSession(token).ifPresent(session -> auditService.record(
                AuditAction.LOGOUT,
                "User",
                String.valueOf(session.getUserId()),
                null,
                Map.of("username", session.getUsername(), "role", session.getRole().name()),
                servletRequest
        ));
        sessions.remove(token);
        revokedTokens.add(token);
    }

    public void cacheUser(AppUserEntity user) {
        users.put(user.getUsername(), new AppUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                Boolean.TRUE.equals(user.getActive())
        ));
    }

    public void evictUser(String username) {
        users.remove(username);
    }
}
