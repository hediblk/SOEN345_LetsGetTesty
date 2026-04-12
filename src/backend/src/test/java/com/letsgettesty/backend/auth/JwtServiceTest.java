package com.letsgettesty.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new ObjectMapper(), "test-secret-key-for-unit-tests", 86400);

    @Test
    void generateTokenProducesThreeSegmentJwt() {
        AccountRecord account = new AccountRecord(1, AccountRole.USER, "Jane Doe", "secret", "jane@example.com", null);

        String token = jwtService.generateToken(account);

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void authenticateReturnsCorrectAccountFromValidToken() {
        AccountRecord account = new AccountRecord(5, AccountRole.ADMIN, "Admin One", "secret", null, "514-555-0100");
        String token = jwtService.generateToken(account);

        AuthenticatedAccount result = jwtService.authenticate(token);

        assertThat(result.id()).isEqualTo(5);
        assertThat(result.role()).isEqualTo(AccountRole.ADMIN);
        assertThat(result.fullName()).isEqualTo("Admin One");
    }

    @Test
    void authenticateThrowsUnauthorizedForNullToken() {
        ResponseStatusException ex = catchThrowableOfType(
                () -> jwtService.authenticate(null),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).isEqualTo("Invalid bearer token.");
    }

    @Test
    void authenticateThrowsUnauthorizedForBlankToken() {
        ResponseStatusException ex = catchThrowableOfType(
                () -> jwtService.authenticate("   "),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticateThrowsUnauthorizedForTokenWithWrongSegmentCount() {
        ResponseStatusException ex = catchThrowableOfType(
                () -> jwtService.authenticate("abc.def"),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticateThrowsUnauthorizedForTamperedSignature() {
        AccountRecord account = new AccountRecord(1, AccountRole.USER, "User", "secret", "u@example.com", null);
        String token = jwtService.generateToken(account);
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "tampered-signature";

        ResponseStatusException ex = catchThrowableOfType(
                () -> jwtService.authenticate(tampered),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticateThrowsUnauthorizedForExpiredToken() {
        JwtService shortLived = new JwtService(new ObjectMapper(), "test-secret-key-for-unit-tests", -1);
        AccountRecord account = new AccountRecord(1, AccountRole.USER, "User", "secret", "u@example.com", null);
        String token = shortLived.generateToken(account);

        ResponseStatusException ex = catchThrowableOfType(
                () -> shortLived.authenticate(token),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).isEqualTo("Bearer token has expired.");
    }

    @Test
    void verifyTokenDoesNotThrowForValidToken() {
        AccountRecord account = new AccountRecord(2, AccountRole.USER, "Jane", "secret", "jane@example.com", null);
        String token = jwtService.generateToken(account);

        jwtService.verifyToken(token);
    }

    @Test
    void generateAndAuthenticateRoundTripForUserRole() {
        AccountRecord account = new AccountRecord(42, AccountRole.USER, "Regular User", "pw", "user@example.com", null);
        String token = jwtService.generateToken(account);

        AuthenticatedAccount result = jwtService.authenticate(token);

        assertThat(result.id()).isEqualTo(42);
        assertThat(result.role()).isEqualTo(AccountRole.USER);
        assertThat(result.fullName()).isEqualTo("Regular User");
    }
}
