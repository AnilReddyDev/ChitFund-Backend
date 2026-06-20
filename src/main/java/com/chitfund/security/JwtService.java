package com.chitfund.security;

import com.chitfund.entity.Role;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final ObjectMapper objectMapper;
    private final String secret;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${app.security.jwt-secret:ChangeMe_JwtSecret_AtLeast32Chars_2026}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters.");
        }
        this.objectMapper = objectMapper;
        this.secret = secret;
    }

    public String generate(UUID userId, String username, Role role, Duration ttl) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("sub", username);
            claims.put("uid", userId.toString());
            claims.put("role", role.name());
            claims.put("exp", Instant.now().plus(ttl).getEpochSecond());

            String headerPart = encode(objectMapper.writeValueAsBytes(header));
            String claimsPart = encode(objectMapper.writeValueAsBytes(claims));
            String signature = sign(headerPart + "." + claimsPart);
            return headerPart + "." + claimsPart + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate token", ex);
        }
    }

    public Optional<AuthenticatedUser> parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !sign(parts[0] + "." + parts[1]).equals(parts[2])) {
                return Optional.empty();
            }
            Map<String, Object> claims = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    }
            );
            long exp = ((Number) claims.get("exp")).longValue();
            if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUser(
                    UUID.fromString((String) claims.get("uid")),
                    (String) claims.get("sub"),
                    Role.valueOf((String) claims.get("role"))
            ));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return encode(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
