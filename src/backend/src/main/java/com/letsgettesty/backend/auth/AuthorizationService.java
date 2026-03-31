package com.letsgettesty.backend.auth;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthorizationService {

    public static final String AUTHENTICATED_ACCOUNT_ATTRIBUTE = "letsgettesty.authenticatedAccount";

    public AuthenticatedAccount requireAuthenticatedAccount(HttpServletRequest request) {
        Object attribute = request.getAttribute(AUTHENTICATED_ACCOUNT_ATTRIBUTE);
        if (attribute instanceof AuthenticatedAccount authenticatedAccount) {
            return authenticatedAccount;
        }
        throw unauthorized("Missing authenticated account.");
    }

    public AuthenticatedAccount requireRole(HttpServletRequest request, AccountRole requiredRole) {
        AuthenticatedAccount authenticatedAccount = requireAuthenticatedAccount(request);
        if (authenticatedAccount.role() != requiredRole) {
            throw forbidden("You do not have permission to access this resource.");
        }
        return authenticatedAccount;
    }

    public AuthenticatedAccount requireAnyRole(HttpServletRequest request, AccountRole... roles) {
        AuthenticatedAccount authenticatedAccount = requireAuthenticatedAccount(request);
        boolean matches = Arrays.stream(roles).anyMatch(role -> role == authenticatedAccount.role());
        if (!matches) {
            throw forbidden("You do not have permission to access this resource.");
        }
        return authenticatedAccount;
    }

    public AuthenticatedAccount requireUserOwnership(HttpServletRequest request, int userId) {
        AuthenticatedAccount authenticatedAccount = requireRole(request, AccountRole.USER);
        if (authenticatedAccount.id() != userId) {
            throw forbidden("You can only access your own reservations.");
        }
        return authenticatedAccount;
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
