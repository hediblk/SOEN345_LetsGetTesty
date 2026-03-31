package com.letsgettesty.backend.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final byte[] signingKey;
    private final long expirationSeconds;

    private static final String ERROR_MSG = "Invalid bearer token.";

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.auth.jwt-secret:${APP_AUTH_JWT_SECRET:letsgettesty-dev-jwt-secret-change-me}}") String jwtSecret,
            @Value("${app.auth.jwt-expiration-seconds:86400}") long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.signingKey = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(AccountRecord account) {
        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT");
        Instant issuedAt = Instant.now();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(account.id()));
        payload.put("role", account.role().name());
        payload.put("name", account.fullName());
        payload.put("iat", issuedAt.getEpochSecond());
        payload.put("exp", issuedAt.plusSeconds(expirationSeconds).getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public AuthenticatedAccount authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw unauthorized(ERROR_MSG);
        }

        String[] segments = token.split("\\.");
        if (segments.length != 3) {
            throw unauthorized(ERROR_MSG);
        }

        String signedContent = segments[0] + "." + segments[1];
        String expectedSignature = sign(signedContent);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                segments[2].getBytes(StandardCharsets.UTF_8))) {
            throw unauthorized(ERROR_MSG);
        }

        Map<String, Object> payload = decodePayload(segments[1]);
        long expiresAt = readLongClaim(payload.get("exp"), ERROR_MSG);
        if (Instant.now().getEpochSecond() >= expiresAt) {
            throw unauthorized("Bearer token has expired.");
        }

        try {
            int accountId = Integer.parseInt(String.valueOf(payload.get("sub")));
            AccountRole role = AccountRole.valueOf(String.valueOf(payload.get("role")));
            String fullName = String.valueOf(payload.get("name"));
            if (fullName.isBlank() || "null".equals(fullName)) {
                throw unauthorized(ERROR_MSG);
            }
            return new AuthenticatedAccount(accountId, role, fullName);
        } catch (RuntimeException exception) {
            throw unauthorized(ERROR_MSG);
        }
    }

    public void verifyToken(String token) {
        authenticate(token);
    }

    private String encodeJson(Map<String, Object> payload) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(payload);
            return URL_ENCODER.encodeToString(bytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not encode JWT content.", exception);
        }
    }

    private Map<String, Object> decodePayload(String encodedPayload) {
        try {
            byte[] decoded = URL_DECODER.decode(encodedPayload);
            return objectMapper.readValue(decoded, MAP_TYPE);
        } catch (Exception exception) {
            throw unauthorized(ERROR_MSG);
        }
    }

    private long readLongClaim(Object value, String message) {
        if (value == null) {
            throw unauthorized(message);
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            throw unauthorized(message);
        }
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
            byte[] signature = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return URL_ENCODER.encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign JWT.", exception);
        }
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
