package com.letsgettesty.backend.auth;

public record AuthResponse(
        int id,
        AccountRole role,
        String fullName,
        String email,
        String phone,
        String contact,
        String contactType,
        String token) {

    public static AuthResponse from(AccountRecord account, String token) {
        String contact = account.email() != null ? account.email() : account.phone();
        String contactType = account.email() != null ? "EMAIL" : "PHONE";
        return new AuthResponse(
                account.id(),
                account.role(),
                account.fullName(),
                account.email(),
                account.phone(),
                contact,
                contactType,
                token);
    }
}
