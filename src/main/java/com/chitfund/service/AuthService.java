package com.chitfund.service;

import com.chitfund.dto.LoginRequest;
import com.chitfund.dto.LoginResponse;
import com.chitfund.security.AppUser;
import com.chitfund.security.TokenSession;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, AppUser> users = new ConcurrentHashMap<>();
    private final Map<String, TokenSession> sessions = new ConcurrentHashMap<>();

    @Value("${app.security.admin-username:admin}")
    private String adminUsername;

    @Value("${app.security.admin-password:ChangeMe_Admin_2026!}")
    private String adminPassword;

    @Value("${app.security.user-username:user}")
    private String userUsername;

    @Value("${app.security.user-password:ChangeMe_User_2026!}")
    private String userPassword;

    @Value("${app.security.token-ttl-minutes:480}")
    private long tokenTtlMinutes;

    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initUsers() {
        requireStrongPassword(adminPassword, "admin");
        requireStrongPassword(userPassword, "user");

        users.put(adminUsername, new AppUser(adminUsername, passwordEncoder.encode(adminPassword), "ADMIN"));
        users.put(userUsername, new AppUser(userUsername, passwordEncoder.encode(userPassword), "USER"));
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = users.get(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = generateToken();
        TokenSession session = new TokenSession(
                token,
                user.getUsername(),
                user.getRole(),
                Instant.now().plus(Duration.ofMinutes(tokenTtlMinutes))
        );
        sessions.put(token, session);

        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    public Optional<TokenSession> findValidSession(String token) {
        TokenSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void requireStrongPassword(String password, String label) {
        if (password == null || password.length() < 12
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalStateException("Configured " + label + " password must be at least 12 characters and include upper, lower, number, and symbol.");
        }
    }
}
