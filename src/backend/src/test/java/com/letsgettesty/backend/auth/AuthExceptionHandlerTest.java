package com.letsgettesty.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler handler = new AuthExceptionHandler();

    @Test
    void handleResponseStatusExceptionReturnsCorrectStatusAndMessage() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid bearer token.");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid bearer token.");
    }

    @Test
    void handleResponseStatusExceptionUsesFallbackMessageWhenReasonIsNull() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Request failed.");
    }

    @Test
    void handleResponseStatusExceptionReturnsForbiddenStatusAndMessage() {
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("You do not have permission to access this resource.");
    }

    @Test
    void handleResponseStatusExceptionReturnsNotFoundStatusAndMessage() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("Event not found.");
    }

    @Test
    void handleResponseStatusExceptionReturnsConflictStatusAndMessage() {
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.CONFLICT, "An account already exists with that email.");

        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("An account already exists with that email.");
    }
}
