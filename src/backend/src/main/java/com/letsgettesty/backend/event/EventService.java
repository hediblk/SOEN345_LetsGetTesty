package com.letsgettesty.backend.event;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.model.Event;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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

    public Event replaceEvent(int id, EventRequest request) {
        Event existing = getEvent(id);
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
}
