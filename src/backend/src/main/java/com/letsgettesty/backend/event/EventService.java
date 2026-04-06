package com.letsgettesty.backend.event;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.Notifier;
import com.letsgettesty.backend.model.Reservation;
import com.letsgettesty.backend.model.ReservationStatus;
import com.letsgettesty.backend.user.UserRepository;
import com.letsgettesty.backend.reservation.ReservationRepository;

@Service
public class EventService {

    private static final DateTimeFormatter EVENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final Notifier notifier;

    public EventService(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            Notifier notifier) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.notifier = notifier;
    }

    public List<Event> listEvents() {
        return eventRepository.findAllOrderedByStartsAt();
    }

    public Event getEvent(int id) {
        return eventRepository
                .findById(id)
                .orElseThrow(() -> notFound("Event not found."));
    }

    public Event createEvent(EventRequest request) {
        validateWrite(request, null);
        Event event = new Event();
        applyRequest(event, request);
        event.setReservedCount(0);
        event.setCancelled(false);
        return eventRepository.insert(event);
    }

    public Event replaceEvent(int id, UpdateEventRequest update) {
        Event existing = getEvent(id);
        if (existing.isCancelled()) {
            throw badRequest("Cancelled events cannot be updated.");
        }
        EventRequest request = update.toEventRequest();
        validateWrite(request, existing);
        Event updated = new Event();
        updated.setId(id);
        applyRequest(updated, request);
        updated.setReservedCount(existing.getReservedCount());
        updated.setCancelled(existing.isCancelled());
        if (!eventRepository.update(updated)) {
            throw notFound("Event not found.");
        }
        return getEvent(id);
    }

    public Event cancelEvent(int id) {
        Event existing = getEvent(id);
        if (existing.isCancelled()) {
            return existing;
        }
        if (!eventRepository.setCancelled(id, true)) {
            throw notFound("Event not found.");
        }
        notifyAttendeesThatEventWasCancelled(existing);
        return getEvent(id);
    }

    private void applyRequest(Event target, EventRequest request) {
        target.setTitle(request.title().trim());
        target.setDescription(normalizeOptional(request.description()));
        target.setCategory(request.category());
        target.setLocation(request.location().trim());
        target.setStartsAt(request.startsAt());
        target.setEndsAt(request.endsAt());
        target.setCapacity(request.capacity());
        target.setPrice(request.price());
    }

    private void validateWrite(EventRequest request, Event existing) {
        if (request.title() == null || request.title().isBlank()) {
            throw badRequest("Title is required.");
        }
        if (request.category() == null) {
            throw badRequest("Category is required.");
        }
        if (request.location() == null || request.location().isBlank()) {
            throw badRequest("Location is required.");
        }
        if (request.startsAt() == null) {
            throw badRequest("Start time is required.");
        }
        if (request.capacity() == null) {
            throw badRequest("Capacity is required.");
        }
        if (request.capacity() < 0) {
            throw badRequest("Capacity cannot be negative.");
        }
        if (request.price() == null) {
            throw badRequest("Price is required.");
        }
        if (request.price() < 0) {
            throw badRequest("Price cannot be negative.");
        }
        if (existing != null && request.capacity() < existing.getReservedCount()) {
            throw badRequest("Capacity cannot be less than the number of reserved seats.");
        }
        if (request.endsAt() != null && request.endsAt().isBefore(request.startsAt())) {
            throw badRequest("End time cannot be before start time.");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private void notifyAttendeesThatEventWasCancelled(Event event) {
        for (Reservation reservation : reservationRepository.findByEventId(event.getId())) {
            if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                continue;
            }

            userRepository.findById(reservation.getUserId())
                    .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
                    .ifPresent(user -> notifier.sendEmail(
                            reservation.getId(),
                            user.getEmail(),
                            "Event cancelled: " + event.getTitle(),
                            """
                                    Hi %s,

                                    "%s" has been cancelled by the organizers.
                                    Reservation ID: %d
                                    Scheduled for: %s
                                    Location: %s

                                    We apologize for the inconvenience.
                                    LetsGetTesty
                                    """.formatted(
                                    user.getFullName(),
                                    event.getTitle(),
                                    reservation.getId(),
                                    formatEventWindow(event),
                                    event.getLocation())));
        }
    }

    private String formatEventWindow(Event event) {
        String start = event.getStartsAt().format(EVENT_TIME_FORMATTER);
        if (event.getEndsAt() == null) {
            return start;
        }
        return start + " to " + event.getEndsAt().format(EVENT_TIME_FORMATTER);
    }
}
