package com.letsgettesty.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

class AuthorizationServiceTest {

    private final AuthorizationService authorizationService = new AuthorizationService();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void requireAuthenticatedAccountReturnsAccountWhenPresent() {
        AuthenticatedAccount account = new AuthenticatedAccount(1, AccountRole.USER, "Jane");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        AuthenticatedAccount result = authorizationService.requireAuthenticatedAccount(request);

        assertThat(result).isEqualTo(account);
    }

    @Test
    void requireAuthenticatedAccountThrowsUnauthorizedWhenMissing() {
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(null);

        ResponseStatusException ex = catchThrowableOfType(
                () -> authorizationService.requireAuthenticatedAccount(request),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).isEqualTo("Missing authenticated account.");
    }

    @Test
    void requireAuthenticatedAccountThrowsUnauthorizedWhenWrongType() {
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn("not-an-account");

        ResponseStatusException ex = catchThrowableOfType(
                () -> authorizationService.requireAuthenticatedAccount(request),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void requireRoleReturnsAccountWhenRoleMatches() {
        AuthenticatedAccount account = new AuthenticatedAccount(1, AccountRole.ADMIN, "Admin One");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        AuthenticatedAccount result = authorizationService.requireRole(request, AccountRole.ADMIN);

        assertThat(result).isEqualTo(account);
    }

    @Test
    void requireRoleThrowsForbiddenWhenRoleMismatch() {
        AuthenticatedAccount account = new AuthenticatedAccount(1, AccountRole.USER, "User One");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        ResponseStatusException ex = catchThrowableOfType(
                () -> authorizationService.requireRole(request, AccountRole.ADMIN),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).isEqualTo("You do not have permission to access this resource.");
    }

    @Test
    void requireAnyRoleReturnsAccountWhenRoleIsInList() {
        AuthenticatedAccount account = new AuthenticatedAccount(2, AccountRole.USER, "User One");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        AuthenticatedAccount result = authorizationService.requireAnyRole(request, AccountRole.USER, AccountRole.ADMIN);

        assertThat(result).isEqualTo(account);
    }

    @Test
    void requireAnyRoleReturnsAccountWhenAdminIsInList() {
        AuthenticatedAccount account = new AuthenticatedAccount(1, AccountRole.ADMIN, "Admin One");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        AuthenticatedAccount result = authorizationService.requireAnyRole(request, AccountRole.USER, AccountRole.ADMIN);

        assertThat(result).isEqualTo(account);
    }

    @Test
    void requireAnyRoleThrowsForbiddenWhenRoleNotInList() {
        AuthenticatedAccount account = new AuthenticatedAccount(2, AccountRole.USER, "User One");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        ResponseStatusException ex = catchThrowableOfType(
                () -> authorizationService.requireAnyRole(request, AccountRole.ADMIN),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).isEqualTo("You do not have permission to access this resource.");
    }

    @Test
    void requireUserOwnershipReturnsAccountWhenUserIdMatches() {
        AuthenticatedAccount account = new AuthenticatedAccount(7, AccountRole.USER, "User Seven");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        AuthenticatedAccount result = authorizationService.requireUserOwnership(request, 7);

        assertThat(result).isEqualTo(account);
    }

    @Test
    void requireUserOwnershipThrowsForbiddenWhenUserIdMismatch() {
        AuthenticatedAccount account = new AuthenticatedAccount(7, AccountRole.USER, "User Seven");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        ResponseStatusException ex = catchThrowableOfType(
                () -> authorizationService.requireUserOwnership(request, 99),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).isEqualTo("You can only access your own reservations.");
    }

    @Test
    void requireUserOwnershipThrowsForbiddenWhenAccountIsAdmin() {
        AuthenticatedAccount account = new AuthenticatedAccount(1, AccountRole.ADMIN, "Admin One");
        when(request.getAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE)).thenReturn(account);

        ResponseStatusException ex = catchThrowableOfType(
                () -> authorizationService.requireUserOwnership(request, 1),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
