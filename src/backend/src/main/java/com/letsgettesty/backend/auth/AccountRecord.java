package com.letsgettesty.backend.auth;

public record AccountRecord(
        int id,
        AccountRole role,
        String fullName,
        String password,
        String email,
        String phone) {
}
