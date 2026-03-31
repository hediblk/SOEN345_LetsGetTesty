package com.letsgettesty.backend.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.event.EventRepository;
import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.EventCategory;
import com.letsgettesty.backend.model.Reservation;
import com.letsgettesty.backend.model.ReservationStatus;

class ReservationServiceTests {

    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final EventRepository eventRepository = mock(EventRepository.class);
    private final ReservationService reservationService = new ReservationService(reservationRepository, eventRepository);

    // --- createReservation ---

    @Test
    void createReservationSucceeds() {
        Event event = new Event(1, "Concert", null, EventCategory.CONCERT, "Venue",
                LocalDateTime.parse("2026-06-01T18:00:00"), null, 100, 20, 5, false);
        Reservation created = new Reservation(1, 42, 1, ReservationStatus.CONFIRMED,
                LocalDateTime.now(), null);

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(reservationRepository.exists(42, 1)).thenReturn(false);
        when(reservationRepository.insert(any(Reservation.class))).thenReturn(created);

        Reservation result = reservationService.createReservation(42, 1);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationRepository).insert(any(Reservation.class));
        verify(eventRepository).update(any(Event.class));
    }

    @Test
    void createReservationIncrementsReservedCount() {
        Event event = new Event(1, "Concert", null, EventCategory.CONCERT, "Venue",
                LocalDateTime.parse("2026-06-01T18:00:00"), null, 100, 20, 5, false);
        Reservation created = new Reservation(1, 42, 1, ReservationStatus.CONFIRMED,
                LocalDateTime.now(), null);

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(reservationRepository.exists(42, 1)).thenReturn(false);
        when(reservationRepository.insert(any(Reservation.class))).thenReturn(created);

        reservationService.createReservation(42, 1);

        verify(eventRepository).update(org.mockito.ArgumentMatchers.argThat(
                e -> e.getReservedCount() == 6));
    }

    @Test
    void createReservationThrowsNotFoundWhenEventMissing() {
        when(eventRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException exception = catchThrowableOfType(
                () -> reservationService.createReservation(1, 999),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Event not found.");
    }

    @Test
    void createReservationRejectsCancelledEvent() {
        Event event = new Event(2, "Gone", null, EventCategory.MOVIE, "Nowhere",
                LocalDateTime.parse("2026-05-01T18:00:00"), null, 50, 10, 0, true);

        when(eventRepository.findById(2)).thenReturn(Optional.of(event));

        ResponseStatusException exception = catchThrowableOfType(
                () -> reservationService.createReservation(1, 2),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Cannot reserve a cancelled event.");
        verify(reservationRepository, never()).insert(any());
    }

    @Test
    void createReservationRejectsFullyBookedEvent() {
        Event event = new Event(3, "Full Show", null, EventCategory.THEATRE, "Stage",
                LocalDateTime.parse("2026-07-01T20:00:00"), null, 50, 15, 50, false);

        when(eventRepository.findById(3)).thenReturn(Optional.of(event));

        ResponseStatusException exception = catchThrowableOfType(
                () -> reservationService.createReservation(1, 3),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Event is fully booked.");
        verify(reservationRepository, never()).insert(any());
    }

    @Test
    void createReservationRejectsDuplicateReservation() {
        Event event = new Event(4, "Art Show", null, EventCategory.ART, "Gallery",
                LocalDateTime.parse("2026-08-01T14:00:00"), null, 30, 0, 10, false);

        when(eventRepository.findById(4)).thenReturn(Optional.of(event));
        when(reservationRepository.exists(7, 4)).thenReturn(true);

        ResponseStatusException exception = catchThrowableOfType(
                () -> reservationService.createReservation(7, 4),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("User already has a reservation for this event.");
        verify(reservationRepository, never()).insert(any());
    }

    // --- cancelReservation ---

    @Test
    void cancelReservationIsIdempotentWhenAlreadyCancelled() {
        Reservation existing = new Reservation(10, 1, 1, ReservationStatus.CANCELLED,
                LocalDateTime.now(), LocalDateTime.now());

        when(reservationRepository.findById(10)).thenReturn(Optional.of(existing));

        Reservation result = reservationService.cancelReservation(10);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(reservationRepository, never()).setCancelled(anyInt());
        verify(eventRepository, never()).update(any());
    }

    @Test
    void cancelReservationDecrementsReservedCount() {
        Reservation existing = new Reservation(11, 2, 5, ReservationStatus.CONFIRMED,
                LocalDateTime.now(), null);
        Event event = new Event(5, "Travel Trip", null, EventCategory.TRAVEL, "Airport",
                LocalDateTime.parse("2026-09-01T08:00:00"), null, 20, 0, 3, false);
        Reservation cancelled = new Reservation(11, 2, 5, ReservationStatus.CANCELLED,
                LocalDateTime.now(), LocalDateTime.now());

        when(reservationRepository.findById(11))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(cancelled));
        when(reservationRepository.setCancelled(11)).thenReturn(true);
        when(eventRepository.findById(5)).thenReturn(Optional.of(event));

        reservationService.cancelReservation(11);

        verify(eventRepository).update(org.mockito.ArgumentMatchers.argThat(
                e -> e.getReservedCount() == 2));
    }

    @Test
    void cancelReservationFloorsReservedCountAtZero() {
        Reservation existing = new Reservation(12, 3, 6, ReservationStatus.CONFIRMED,
                LocalDateTime.now(), null);
        Event event = new Event(6, "Sport Event", null, EventCategory.SPORT, "Stadium",
                LocalDateTime.parse("2026-10-01T15:00:00"), null, 200, 5, 0, false);
        Reservation cancelled = new Reservation(12, 3, 6, ReservationStatus.CANCELLED,
                LocalDateTime.now(), LocalDateTime.now());

        when(reservationRepository.findById(12))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.of(cancelled));
        when(reservationRepository.setCancelled(12)).thenReturn(true);
        when(eventRepository.findById(6)).thenReturn(Optional.of(event));

        reservationService.cancelReservation(12);

        verify(eventRepository).update(org.mockito.ArgumentMatchers.argThat(
                e -> e.getReservedCount() == 0));
    }

    @Test
    void cancelReservationThrowsNotFoundWhenMissing() {
        when(reservationRepository.findById(999)).thenReturn(Optional.empty());

        ResponseStatusException exception = catchThrowableOfType(
                () -> reservationService.cancelReservation(999),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Reservation not found.");
    }

    // --- getReservation ---

    @Test
    void getReservationThrowsNotFoundWhenMissing() {
        when(reservationRepository.findById(404)).thenReturn(Optional.empty());

        ResponseStatusException exception = catchThrowableOfType(
                () -> reservationService.getReservation(404),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Reservation not found.");
    }
}
