package com.letsgettesty.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {

    private final AuthRepository authRepository = mock(AuthRepository.class);
    private final AuthService authService = new AuthService(authRepository);

    @Test
    void registerDefaultsToUserAndUsesFrontendContactFields() {
        when(authRepository.existsByFullName("Jane Doe")).thenReturn(false);
        when(authRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(authRepository.createAccount(
                AccountRole.USER,
                "Jane Doe",
                "secret",
                "jane@example.com",
                null)).thenReturn(new AccountRecord(
                        7,
                        AccountRole.USER,
                        "Jane Doe",
                        "secret",
                        "jane@example.com",
                        null));

        AuthResponse response = authService.register(
                new RegisterRequest(null, "Jane Doe", "secret", null, null, "jane@example.com", "email"));

        assertThat(response.role()).isEqualTo(AccountRole.USER);
        assertThat(response.fullName()).isEqualTo("Jane Doe");
        assertThat(response.email()).isEqualTo("jane@example.com");
        assertThat(response.phone()).isNull();
        assertThat(response.contactType()).isEqualTo("EMAIL");

        verify(authRepository).createAccount(AccountRole.USER, "Jane Doe", "secret", "jane@example.com", null);
    }

    @Test
    void registerRejectsProvidingEmailAndPhoneTogether() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> authService.register(
                        new RegisterRequest(
                                null,
                                "Jane Doe",
                                "secret",
                                "jane@example.com",
                                "514-555-0100",
                                null,
                                null)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Provide either email or phone, but not both.");
    }

    @Test
    void registerRejectsDuplicatePhoneNumber() {
        when(authRepository.existsByFullName("Jane Doe")).thenReturn(false);
        when(authRepository.existsByPhone("514-555-0100")).thenReturn(true);

        ResponseStatusException exception = catchThrowableOfType(
                () -> authService.register(
                        new RegisterRequest(
                                "ADMIN",
                                "Jane Doe",
                                "secret",
                                null,
                                null,
                                "514-555-0100",
                                "phone")),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("An account already exists with that phone number.");
    }

    @Test
    void loginAcceptsPhoneNumberForAdminAccounts() {
        when(authRepository.findByLoginContact("514-555-0100")).thenReturn(List.of(
                new AccountRecord(
                        3,
                        AccountRole.ADMIN,
                        "Admin One",
                        "admin123",
                        null,
                        "514-555-0100")));

        AuthResponse response = authService.login(
                new LoginRequest("514-555-0100", null, null, "admin123", "phone"));

        assertThat(response.role()).isEqualTo(AccountRole.ADMIN);
        assertThat(response.contact()).isEqualTo("514-555-0100");
        assertThat(response.contactType()).isEqualTo("PHONE");
    }

    @Test
    void loginRejectsWrongPassword() {
        when(authRepository.findByLoginContact("user1@example.com")).thenReturn(List.of(
                new AccountRecord(
                        1,
                        AccountRole.USER,
                        "user1",
                        "user123",
                        "user1@example.com",
                        null)));

        ResponseStatusException exception = catchThrowableOfType(
                () -> authService.login(
                        new LoginRequest("user1@example.com", null, null, "wrong", "email")),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getReason()).isEqualTo("Invalid password.");
    }
}
