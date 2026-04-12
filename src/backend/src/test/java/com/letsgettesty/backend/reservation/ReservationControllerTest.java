package com.letsgettesty.backend.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.auth.AccountRole;
import com.letsgettesty.backend.auth.AuthenticatedAccount;
import com.letsgettesty.backend.auth.AuthExceptionHandler;
import com.letsgettesty.backend.auth.AuthorizationService;
import com.letsgettesty.backend.auth.JwtAuthInterceptor;
import com.letsgettesty.backend.auth.JwtService;
import com.letsgettesty.backend.model.Reservation;
import com.letsgettesty.backend.model.ReservationStatus;

@WebMvcTest(ReservationController.class)
@Import({ AuthExceptionHandler.class, JwtAuthInterceptor.class })
class ReservationControllerTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN = "user-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        when(jwtService.authenticate(ADMIN_TOKEN))
                .thenReturn(new AuthenticatedAccount(1, AccountRole.ADMIN, "Admin One"));
        when(jwtService.authenticate(USER_TOKEN))
                .thenReturn(new AuthenticatedAccount(2, AccountRole.USER, "User One"));
    }

    private Reservation reservation(int id, int userId) {
        return new Reservation(id, userId, 10, ReservationStatus.CONFIRMED,
                LocalDateTime.parse("2026-01-01T10:00:00"), null);
    }

    // --- GET /api/reservations ---

    @Test
    void listReservationsRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", equalTo("Missing bearer token.")));
    }

    @Test
    void listReservationsReturnsListForAdmin() throws Exception {
        when(authorizationService.requireRole(any(), eq(AccountRole.ADMIN)))
                .thenReturn(new AuthenticatedAccount(1, AccountRole.ADMIN, "Admin One"));
        when(reservationService.listReservations()).thenReturn(List.of(reservation(1, 2), reservation(2, 3)));

        mockMvc.perform(get("/api/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listReservationsForbidsRegularUser() throws Exception {
        when(authorizationService.requireRole(any(), eq(AccountRole.ADMIN)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You do not have permission to access this resource."));

        mockMvc.perform(get("/api/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", equalTo("You do not have permission to access this resource.")));
    }

    // --- GET /api/reservations/{id} ---

    @Test
    void getReservationReturnsReservationForOwner() throws Exception {
        Reservation r = reservation(5, 2);
        when(authorizationService.requireAnyRole(any(), eq(AccountRole.USER), eq(AccountRole.ADMIN)))
                .thenReturn(new AuthenticatedAccount(2, AccountRole.USER, "User One"));
        when(reservationService.getReservation(5)).thenReturn(r);

        mockMvc.perform(get("/api/reservations/5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    void getReservationReturnsNotFoundWhenMissing() throws Exception {
        when(authorizationService.requireAnyRole(any(), eq(AccountRole.USER), eq(AccountRole.ADMIN)))
                .thenReturn(new AuthenticatedAccount(2, AccountRole.USER, "User One"));
        when(reservationService.getReservation(999))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found."));

        mockMvc.perform(get("/api/reservations/999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", equalTo("Reservation not found.")));
    }

    @Test
    void getReservationForbidsUserAccessingOtherUsersReservation() throws Exception {
        Reservation r = reservation(5, 99);
        when(authorizationService.requireAnyRole(any(), eq(AccountRole.USER), eq(AccountRole.ADMIN)))
                .thenReturn(new AuthenticatedAccount(2, AccountRole.USER, "User One"));
        when(reservationService.getReservation(5)).thenReturn(r);
        when(authorizationService.requireUserOwnership(any(), eq(99)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only access your own reservations."));

        mockMvc.perform(get("/api/reservations/5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", equalTo("You can only access your own reservations.")));
    }

    // --- GET /api/reservations/user/{userId} ---

    @Test
    void listByUserReturnsReservationsForOwner() throws Exception {
        when(authorizationService.requireUserOwnership(any(), eq(2)))
                .thenReturn(new AuthenticatedAccount(2, AccountRole.USER, "User One"));
        when(reservationService.listByUser(2)).thenReturn(List.of(reservation(3, 2)));

        mockMvc.perform(get("/api/reservations/user/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(2));
    }

    @Test
    void listByUserForbidsOtherUsers() throws Exception {
        when(authorizationService.requireUserOwnership(any(), eq(99)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only access your own reservations."));

        mockMvc.perform(get("/api/reservations/user/99")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", equalTo("You can only access your own reservations.")));
    }

    // --- GET /api/reservations/event/{eventId} ---

    @Test
    void listByEventReturnsListForAdmin() throws Exception {
        when(authorizationService.requireRole(any(), eq(AccountRole.ADMIN)))
                .thenReturn(new AuthenticatedAccount(1, AccountRole.ADMIN, "Admin One"));
        when(reservationService.listByEvent(10)).thenReturn(List.of(reservation(7, 2)));

        mockMvc.perform(get("/api/reservations/event/10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventId").value(10));
    }

    @Test
    void listByEventForbidsRegularUser() throws Exception {
        when(authorizationService.requireRole(any(), eq(AccountRole.ADMIN)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You do not have permission to access this resource."));

        mockMvc.perform(get("/api/reservations/event/10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/reservations ---

    @Test
    void createReservationReturns201() throws Exception {
        Reservation created = reservation(8, 2);
        when(authorizationService.requireUserOwnership(any(), eq(2)))
                .thenReturn(new AuthenticatedAccount(2, AccountRole.USER, "User One"));
        when(reservationService.createReservation(2, 10)).thenReturn(created);

        mockMvc.perform(post("/api/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"userId": 2, "eventId": 10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createReservationForbidsCreatingForOtherUser() throws Exception {
        when(authorizationService.requireUserOwnership(any(), eq(99)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only access your own reservations."));

        mockMvc.perform(post("/api/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"userId": 99, "eventId": 10}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReservationReturnsBadRequestWhenEventFullyBooked() throws Exception {
        when(authorizationService.requireUserOwnership(any(), eq(2)))
                .thenReturn(new AuthenticatedAccount(2, AccountRole.USER, "User One"));
        when(reservationService.createReservation(2, 10))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is fully booked."));

        mockMvc.perform(post("/api/reservations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"userId": 2, "eventId": 10}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Event is fully booked.")));
    }

    // --- PATCH /api/reservations/{id}/cancel ---

    @Test
    void cancelReservationReturnsUpdatedReservation() throws Exception {
        Reservation existing = reservation(3, 2);
        Reservation cancelled = new Reservation(3, 2, 10, ReservationStatus.CANCELLED,
                LocalDateTime.parse("2026-01-01T10:00:00"), LocalDateTime.parse("2026-01-02T09:00:00"));
        when(reservationService.getReservation(3)).thenReturn(existing);
        when(authorizationService.requireUserOwnership(any(), eq(2)))
                .thenReturn(new AuthenticatedAccount(2, AccountRole.USER, "User One"));
        when(reservationService.cancelReservation(3)).thenReturn(cancelled);

        mockMvc.perform(patch("/api/reservations/3/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelReservationForbidsOtherUsers() throws Exception {
        Reservation existing = reservation(3, 99);
        when(reservationService.getReservation(3)).thenReturn(existing);
        when(authorizationService.requireUserOwnership(any(), eq(99)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only access your own reservations."));

        mockMvc.perform(patch("/api/reservations/3/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", equalTo("You can only access your own reservations.")));
    }

    @Test
    void cancelReservationReturnsNotFoundWhenMissing() throws Exception {
        when(reservationService.getReservation(404))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found."));

        mockMvc.perform(patch("/api/reservations/404/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", equalTo("Reservation not found.")));
    }
}
