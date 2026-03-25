package com.letsgettesty.backend.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.letsgettesty.backend.model.Event;
import com.letsgettesty.backend.model.EventCategory;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class EventDatabaseIntegrationTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void replaceEventUpdatesFieldsButKeepsReservedCountAndCancelledFlag() {
        Event before = eventService.getEvent(2);
        assertThat(before.isCancelled()).isFalse();
        assertThat(before.getReservedCount()).isGreaterThanOrEqualTo(0);

        int newCapacity = Math.max(before.getReservedCount(), 0) + 50;
        Event updated = eventService.replaceEvent(
                2,
                new UpdateEventRequest(
                        "Updated title (integration)",
                        before.getDescription(),
                        EventCategory.valueOf(before.getCategory().name()),
                        before.getLocation(),
                        before.getStartsAt(),
                        null,
                        before.getEndsAt(),
                        newCapacity,
                        before.getPrice()));

        Event after = eventService.getEvent(2);
        assertThat(after.getTitle()).isEqualTo(updated.getTitle());
        assertThat(after.getReservedCount()).isEqualTo(before.getReservedCount());
        assertThat(after.isCancelled()).isEqualTo(before.isCancelled());
    }

    @Test
    void cancelEventSoftCancelsWithoutDeleting() {
        Long countBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class);

        Event after = eventService.cancelEvent(2);
        assertThat(after.isCancelled()).isTrue();

        Long countAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class);
        assertThat(countAfter).isEqualTo(countBefore);

        Optional<Event> fromRepo = eventRepository.findById(2);
        assertThat(fromRepo).isPresent();
        assertThat(fromRepo.get().isCancelled()).isTrue();
    }

    @Test
    void cancelNonExistingEventReturnsNotFound() {
        ResponseStatusException exception = catchThrowableOfType(
                () -> eventService.cancelEvent(99999),
                ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Event not found.");
    }
}

