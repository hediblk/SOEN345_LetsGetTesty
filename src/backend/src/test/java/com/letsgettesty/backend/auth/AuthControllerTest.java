package com.letsgettesty.backend.auth;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@WebMvcTest(AuthController.class)
@Import(AuthExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void registerAcceptsFrontendPayloadShape() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(
                new AuthResponse(
                        1,
                        AccountRole.USER,
                        "Jane Doe",
                        "jane@example.com",
                        null,
                        "jane@example.com",
                        "EMAIL"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Jane Doe",
                                  "contact": "jane@example.com",
                                  "contactType": "email",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role", equalTo("USER")))
                .andExpect(jsonPath("$.fullName", equalTo("Jane Doe")))
                .andExpect(jsonPath("$.contact", equalTo("jane@example.com")))
                .andExpect(jsonPath("$.contactType", equalTo("EMAIL")));

        verify(authService).register(argThat(request ->
                "Jane Doe".equals(request.fullName())
                        && "jane@example.com".equals(request.contact())
                        && "email".equals(request.contactType())
                        && "secret".equals(request.password())));
    }

    @Test
    void loginReturnsHandledErrorsAsJson() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "contact": "514-555-0100",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", equalTo("Invalid password.")));
    }
}
