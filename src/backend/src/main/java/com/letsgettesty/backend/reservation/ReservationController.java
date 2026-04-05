package com.letsgettesty.backend.reservation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.letsgettesty.backend.auth.AccountRole;
import com.letsgettesty.backend.auth.AuthenticatedAccount;
import com.letsgettesty.backend.auth.AuthorizationService;
import com.letsgettesty.backend.model.Reservation;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" })
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthorizationService authorizationService;

    public ReservationController(ReservationService reservationService, AuthorizationService authorizationService) {
        this.reservationService = reservationService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<ReservationResponse> listReservations(HttpServletRequest request) {
        authorizationService.requireRole(request, AccountRole.ADMIN);
        return reservationService.listReservations().stream().map(ReservationResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable int id, HttpServletRequest request) {
        AuthenticatedAccount authenticatedAccount = authorizationService.requireAnyRole(
                request,
                AccountRole.USER,
                AccountRole.ADMIN);
        Reservation reservation = reservationService.getReservation(id);
        if (authenticatedAccount.role() == AccountRole.USER && authenticatedAccount.id() != reservation.getUserId()) {
            authorizationService.requireUserOwnership(request, reservation.getUserId());
        }
        return ReservationResponse.from(reservation);
    }

    @GetMapping("/user/{userId}")
    public List<ReservationResponse> listByUser(@PathVariable int userId, HttpServletRequest request) {
        authorizationService.requireUserOwnership(request, userId);
        return reservationService.listByUser(userId).stream().map(ReservationResponse::from).toList();
    }

    @GetMapping("/event/{eventId}")
    public List<ReservationResponse> listByEvent(@PathVariable int eventId, HttpServletRequest request) {
        authorizationService.requireRole(request, AccountRole.ADMIN);
        return reservationService.listByEvent(eventId).stream().map(ReservationResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationRequest reservationRequest,
            HttpServletRequest request) {
        authorizationService.requireUserOwnership(request, reservationRequest.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(
                        reservationService.createReservation(reservationRequest.userId(), reservationRequest.eventId())));
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancelReservation(@PathVariable int id, HttpServletRequest request) {
        Reservation reservation = reservationService.getReservation(id);
        authorizationService.requireUserOwnership(request, reservation.getUserId());
        return ReservationResponse.from(reservationService.cancelReservation(id));
    }
}
