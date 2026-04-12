package com.letsgettesty.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JwtAuthInterceptorTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtAuthInterceptor interceptor = new JwtAuthInterceptor(jwtService);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @Test
    void preHandleReturnsTrueForOptionsRequest() throws Exception {
        when(request.getMethod()).thenReturn("OPTIONS");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }

    @Test
    void preHandleThrowsUnauthorizedWhenAuthorizationHeaderIsMissing() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseStatusException ex = catchThrowableOfType(
                () -> interceptor.preHandle(request, response, new Object()),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).isEqualTo("Missing bearer token.");
    }

    @Test
    void preHandleThrowsUnauthorizedWhenHeaderDoesNotStartWithBearer() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        ResponseStatusException ex = catchThrowableOfType(
                () -> interceptor.preHandle(request, response, new Object()),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).isEqualTo("Missing bearer token.");
    }

    @Test
    void preHandleSetsAuthenticatedAccountAttributeOnSuccess() throws Exception {
        AuthenticatedAccount account = new AuthenticatedAccount(1, AccountRole.USER, "Jane");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.authenticate("valid-token")).thenReturn(account);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(request).setAttribute(AuthorizationService.AUTHENTICATED_ACCOUNT_ATTRIBUTE, account);
    }

    @Test
    void preHandleStripsWhitespaceFromTokenBeforeAuthenticating() throws Exception {
        AuthenticatedAccount account = new AuthenticatedAccount(2, AccountRole.ADMIN, "Admin");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer   spaced-token  ");
        when(jwtService.authenticate("spaced-token")).thenReturn(account);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(jwtService).authenticate("spaced-token");
    }

    @Test
    void preHandleThrowsWhenJwtServiceThrows() {
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtService.authenticate("bad-token"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid bearer token."));

        ResponseStatusException ex = catchThrowableOfType(
                () -> interceptor.preHandle(request, response, new Object()),
                ResponseStatusException.class);

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getReason()).isEqualTo("Invalid bearer token.");
    }
}
