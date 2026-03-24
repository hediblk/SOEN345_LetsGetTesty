package com.letsgettesty.backend.auth;

import com.fasterxml.jackson.annotation.JsonAlias;

public record LoginRequest(
        @JsonAlias("identifier") String contact,
        String email,
        String phone,
        String password,
        String contactType) {
}
