package com.letsgettesty.backend.reservation;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.event.EventRepository;
import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.Notifier;
import com.letsgettesty.backend.model.Reservation;
import com.letsgettesty.backend.model.ReservationStatus;
import com.letsgettesty.backend.model.User;
import com.letsgettesty.backend.user.UserRepository;

@Service
public class ReservationService {

    private static final DateTimeFormatter EVENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final Notifier notifier;

    public ReservationService(
            ReservationRepository reservationRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            Notifier notifier) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notifier = notifier;
    }

    public List<Reservation> listReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservation(int id) {
        return reservationRepository
                .findById(id)
                .orElseThrow(() -> notFound("Reservation not found."));
    }

    public List<Reservation> listByUser(int userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<Reservation> listByEvent(int eventId) {
        return reservationRepository.findByEventId(eventId);
    }

    public Reservation createReservation(int userId, int eventId) {
        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> notFound("Event not found."));

        if (event.isCancelled()) {
            throw badRequest("Cannot reserve a cancelled event.");
        }
        if (event.getReservedCount() >= event.getCapacity()) {
            throw badRequest("Event is fully booked.");
        }
        if (reservationRepository.exists(userId, eventId)) {
            throw badRequest("User already has a reservation for this event.");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setEventId(eventId);

        Reservation created = reservationRepository.insert(reservation);

        event.setReservedCount(event.getReservedCount() + 1);
        eventRepository.update(event);
        sendRegistrationCreatedEmail(created, event);

        return created;
    }

    public Reservation cancelReservation(int id) {
        Reservation existing = getReservation(id);

        if (existing.getStatus() == ReservationStatus.CANCELLED) {
            return existing;
        }

        if (!reservationRepository.setCancelled(id)) {
            throw notFound("Reservation not found.");
        }

        eventRepository.findById(existing.getEventId()).ifPresent(event -> {
            event.setReservedCount(Math.max(0, event.getReservedCount() - 1));
            eventRepository.update(event);
            sendRegistrationCancelledEmail(existing, event);
        });

        return getReservation(id);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private void sendRegistrationCreatedEmail(Reservation reservation, Event event) {
        lookupUserWithEmail(reservation.getUserId()).ifPresent(user -> notifier.sendEmail(
                reservation.getId(),
                user.getEmail(),
                "Registration confirmed: " + event.getTitle(),
                """
                        Hi %s,

                        Your registration for "%s" is confirmed.
                        Reservation ID: %d
                        When: %s
                        Where: %s

                        See you there,
                        LetsGetTesty
                        """.formatted(
                        user.getFullName(),
                        event.getTitle(),
                        reservation.getId(),
                        formatEventWindow(event),
                        event.getLocation())));
    }

    private void sendRegistrationCancelledEmail(Reservation reservation, Event event) {
        lookupUserWithEmail(reservation.getUserId()).ifPresent(user -> notifier.sendEmail(
                reservation.getId(),
                user.getEmail(),
                "Registration cancelled: " + event.getTitle(),
                """
                        Hi %s,

                        Your registration for "%s" has been cancelled.
                        Reservation ID: %d
                        Event time: %s
                        Location: %s

                        Regards,
                        LetsGetTesty
                        """.formatted(
                        user.getFullName(),
                        event.getTitle(),
                        reservation.getId(),
                        formatEventWindow(event),
                        event.getLocation())));
    }

    private java.util.Optional<User> lookupUserWithEmail(int userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getEmail() != null && !user.getEmail().isBlank());
    }

    private String formatEventWindow(Event event) {
        String start = event.getStartsAt().format(EVENT_TIME_FORMATTER);
        if (event.getEndsAt() == null) {
            return start;
        }
        return start + " to " + event.getEndsAt().format(EVENT_TIME_FORMATTER);
    }
}
