package com.letsgettesty.backend.auth;

public record AuthenticatedAccount(
        int id,
        AccountRole role,
        String fullName) {
}
