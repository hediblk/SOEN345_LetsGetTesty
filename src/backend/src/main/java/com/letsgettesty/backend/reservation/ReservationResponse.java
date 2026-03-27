package com.letsgettesty.backend.reservation;

import java.time.LocalDateTime;

import com.letsgettesty.backend.model.Reservation;
import com.letsgettesty.backend.model.ReservationStatus;

public record ReservationResponse(
        int id,
        int userId,
        int eventId,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getEventId(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getCancelledAt());
    }
}