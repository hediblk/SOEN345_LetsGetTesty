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

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:5173")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> listReservations() {
        return reservationService.listReservations().stream().map(ReservationResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ReservationResponse getReservation(@PathVariable int id) {
        return ReservationResponse.from(reservationService.getReservation(id));
    }

    @GetMapping("/user/{userId}")
    public List<ReservationResponse> listByUser(@PathVariable int userId) {
        return reservationService.listByUser(userId).stream().map(ReservationResponse::from).toList();
    }

    @GetMapping("/event/{eventId}")
    public List<ReservationResponse> listByEvent(@PathVariable int eventId) {
        return reservationService.listByEvent(eventId).stream().map(ReservationResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservationService.createReservation(request.userId(), request.eventId())));
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancelReservation(@PathVariable int id) {
        return ReservationResponse.from(reservationService.cancelReservation(id));
    }
}