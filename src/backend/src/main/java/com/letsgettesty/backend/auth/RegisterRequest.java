package com.letsgettesty.backend.auth;

import com.fasterxml.jackson.annotation.JsonAlias;

public record RegisterRequest(
        String role,
        @JsonAlias("name") String fullName,
        String password,
        String email,
        String phone,
        String contact,
        String contactType) {
}
