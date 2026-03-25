package com.letsgettesty.backend.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.EventCategory;

class EventServiceTest {

    private final EventRepository eventRepository = mock(EventRepository.class);
    private final EventService eventService = new EventService(eventRepository);

    @Test
    void createEventRequiresPrice() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest(
                                "Title",
                                null,
                                EventCategory.MOVIE,
                                "Place",
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                10,
                                null)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Price is required.");
    }

    @Test
    void createEventRejectsNegativePrice() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest(
                                "Title",
                                null,
                                EventCategory.MOVIE,
                                "Place",
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                10,
                                -1)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Price cannot be negative.");
    }

    @Test
    void createEventRequiresCategory() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest(
                                "Title",
                                null,
                                null,
                                "Place",
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                10,
                                0)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Category is required.");
    }

    @Test
    void createEventRequiresLocation() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest(
                                "Title",
                                null,
                                EventCategory.MOVIE,
                                null,
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                10,
                                0)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Location is required.");
    }

    @Test
    void createEventRequiresStartsAt() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest(
                                "Title",
                                null,
                                EventCategory.MOVIE,
                                "Place",
                                null,
                                null,
                                10,
                                0)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Start time is required.");
    }

    @Test
    void createEventRequiresCapacity() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest(
                                "Title",
                                null,
                                EventCategory.MOVIE,
                                "Place",
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                null,
                                0)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Capacity is required.");
    }

    @Test
    void createEventRejectsNegativeCapacity() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest(
                                "Title",
                                null,
                                EventCategory.MOVIE,
                                "Place",
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                -1,
                                0)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Capacity cannot be negative.");
    }

    @Test
    void createEventDelegatesToRepository() {
        Event saved = new Event(
                1,
                "Title",
                null,
                EventCategory.TRAVEL,
                "Airport",
                LocalDateTime.parse("2026-02-02T15:00:00"),
                null,
                50,
                99,
                0,
                false);
        when(eventRepository.insert(any(Event.class))).thenReturn(saved);

        Event result = eventService.createEvent(
                new EventRequest(
                        "Title",
                        null,
                        EventCategory.TRAVEL,
                        "Airport",
                        LocalDateTime.parse("2026-02-02T15:00:00"),
                        null,
                        50,
                        99));

        assertThat(result.getPrice()).isEqualTo(99);
        assertThat(result.getReservedCount()).isEqualTo(0);
        verify(eventRepository).insert(any(Event.class));
    }

    @Test
    void createEventRequiresTitle() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest(
                                "   ",
                                null,
                                EventCategory.MOVIE,
                                "Place",
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                10,
                                0)),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Title is required.");
    }

    @Test
    void createEventRejectsEndBeforeStart() {
        LocalDateTime start = LocalDateTime.parse("2026-06-01T18:00:00");
        LocalDateTime end = LocalDateTime.parse("2026-06-01T12:00:00");
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.createEvent(
                        new EventRequest("Show", null, EventCategory.CONCERT, "Venue", start, end, 50, 10)),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("End time cannot be before start time.");
    }

    @Test
    void replaceEventRejectsCapacityBelowReserved() {
        Event existing = new Event(
                1,
                "Old",
                null,
                EventCategory.MOVIE,
                "Here",
                LocalDateTime.parse("2026-01-01T12:00:00"),
                null,
                100,
                0,
                40,
                false);
        when(eventRepository.findById(1)).thenReturn(java.util.Optional.of(existing));

        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.replaceEvent(
                        1,
                        new UpdateEventRequest(
                                "Old",
                                null,
                                EventCategory.MOVIE,
                                "Here",
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                null,
                                30,
                                0)),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Capacity cannot be less than the number of reserved seats.");
    }

    @Test
    void replaceEventRejectsEndBeforeStart() {
        Event existing = new Event(
                1,
                "Old",
                null,
                EventCategory.MOVIE,
                "Here",
                LocalDateTime.parse("2026-01-01T12:00:00"),
                null,
                100,
                0,
                0,
                false);
        when(eventRepository.findById(1)).thenReturn(java.util.Optional.of(existing));

        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.replaceEvent(
                        1,
                        new UpdateEventRequest(
                                "Old",
                                null,
                                EventCategory.MOVIE,
                                "Here",
                                LocalDateTime.parse("2026-06-01T18:00:00"),
                                null,
                                LocalDateTime.parse("2026-06-01T12:00:00"),
                                10,
                                0)),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("End time cannot be before start time.");
    }

    @Test
    void cancelEventReturnsNotFoundWhenMissing() {
        when(eventRepository.findById(999)).thenReturn(java.util.Optional.empty());

        ResponseStatusException exception = catchThrowableOfType(() -> eventService.cancelEvent(999),
                ResponseStatusException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Event not found.");
    }

    @Test
    void replaceEventRejectsWhenCancelled() {
        Event existing = new Event(
                7,
                "Cancelled show",
                null,
                EventCategory.THEATRE,
                "Venue",
                LocalDateTime.parse("2026-05-05T12:00:00"),
                null,
                50,
                10,
                0,
                true);
        when(eventRepository.findById(7)).thenReturn(java.util.Optional.of(existing));

        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.replaceEvent(
                        7,
                        new UpdateEventRequest(
                                "New title",
                                null,
                                EventCategory.THEATRE,
                                "Venue",
                                LocalDateTime.parse("2026-05-05T12:00:00"),
                                null,
                                null,
                                50,
                                10)),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("Cancelled events cannot be updated.");
        verify(eventRepository, never()).update(any(Event.class));
    }

    @Test
    void replaceEventPersistsViaRepository() {
        Event existing = new Event(
                8,
                "Before",
                null,
                EventCategory.MOVIE,
                "Cinema",
                LocalDateTime.parse("2026-06-01T18:00:00"),
                null,
                80,
                15,
                5,
                false);
        Event afterUpdate = new Event(
                8,
                "After",
                "Desc",
                EventCategory.MOVIE,
                "Cinema",
                LocalDateTime.parse("2026-06-02T19:00:00"),
                null,
                80,
                15,
                5,
                false);
        when(eventRepository.findById(8)).thenReturn(java.util.Optional.of(existing)).thenReturn(java.util.Optional.of(afterUpdate));
        when(eventRepository.update(any(Event.class))).thenReturn(true);

        Event result = eventService.replaceEvent(
                8,
                new UpdateEventRequest(
                        "After",
                        "Desc",
                        EventCategory.MOVIE,
                        "Cinema",
                        LocalDateTime.parse("2026-06-02T19:00:00"),
                        null,
                        null,
                        80,
                        15));

        assertThat(result.getTitle()).isEqualTo("After");
        assertThat(result.getReservedCount()).isEqualTo(5);
        verify(eventRepository).update(any(Event.class));
    }

    @Test
    void replaceEventUsesDateFieldAt1800WhenStartsAtOmitted() {
        Event existing = new Event(
                10,
                "E",
                null,
                EventCategory.TRAVEL,
                "Airport",
                LocalDateTime.parse("2026-01-10T18:00:00"),
                null,
                20,
                0,
                0,
                false);
        Event after = new Event(
                10,
                "E",
                null,
                EventCategory.TRAVEL,
                "Airport",
                LocalDate.parse("2026-12-12").atTime(18, 0),
                null,
                20,
                0,
                0,
                false);
        when(eventRepository.findById(10)).thenReturn(java.util.Optional.of(existing)).thenReturn(java.util.Optional.of(after));
        when(eventRepository.update(any(Event.class))).thenReturn(true);

        eventService.replaceEvent(
                10,
                new UpdateEventRequest(
                        "E",
                        null,
                        EventCategory.TRAVEL,
                        "Airport",
                        null,
                        LocalDate.parse("2026-12-12"),
                        null,
                        20,
                        0));

        verify(eventRepository)
                .update(
                        org.mockito.ArgumentMatchers.argThat(
                                e -> e.getStartsAt().equals(LocalDate.parse("2026-12-12").atTime(18, 0))));
    }

    @Test
    void replaceEventThrowsNotFoundWhenMissing() {
        when(eventRepository.findById(404)).thenReturn(java.util.Optional.empty());

        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.replaceEvent(
                        404,
                        new UpdateEventRequest(
                                "X",
                                null,
                                EventCategory.MOVIE,
                                "L",
                                LocalDateTime.parse("2026-01-01T12:00:00"),
                                null,
                                null,
                                1,
                                0)),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Event not found.");
    }

    @Test
    void getEventThrowsNotFoundWhenMissing() {
        when(eventRepository.findById(999)).thenReturn(java.util.Optional.empty());

        ResponseStatusException exception =
                catchThrowableOfType(() -> eventService.getEvent(999), ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Event not found.");
    }

    @Test
    void cancelEventReturnsExistingWhenAlreadyCancelled() {
        Event existing = new Event(
                5,
                "Gone",
                null,
                EventCategory.OTHERS,
                "Nowhere",
                LocalDateTime.parse("2026-03-03T12:00:00"),
                null,
                10,
                0,
                0,
                true);
        when(eventRepository.findById(5)).thenReturn(java.util.Optional.of(existing));

        Event result = eventService.cancelEvent(5);

        assertThat(result.isCancelled()).isTrue();
        verify(eventRepository).findById(5);
        verify(eventRepository, never()).setCancelled(anyInt(), anyBoolean());
    }
}
