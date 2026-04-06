package com.letsgettesty.backend.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.List;
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

    private static final int EVENT_ID_FOR_CANCEL = 2;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void replaceEventUpdatesFieldsButKeepsReservedCountAndCancelledFlag() {
        List<Integer> activeIds =
                jdbcTemplate.query("SELECT id FROM events WHERE NOT is_cancelled ORDER BY id", (rs, row) -> rs.getInt(1));
        assertThat(activeIds)
                .as("need at least one non-cancelled event (re-seed or fix is_cancelled in DB)")
                .isNotEmpty();
        int replaceTargetId = activeIds.stream()
                .filter(id -> id != EVENT_ID_FOR_CANCEL)
                .findFirst()
                .orElseGet(() -> activeIds.get(0));

        Event before = eventService.getEvent(replaceTargetId);
        assertThat(before.isCancelled()).isFalse();
        assertThat(before.getReservedCount()).isGreaterThanOrEqualTo(0);

        int newCapacity = Math.max(before.getReservedCount(), 0) + 50;
        Event updated = eventService.replaceEvent(
                replaceTargetId,
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

        Event after = eventService.getEvent(replaceTargetId);
        assertThat(after.getTitle()).isEqualTo(updated.getTitle());
        assertThat(after.getReservedCount()).isEqualTo(before.getReservedCount());
        assertThat(after.isCancelled()).isEqualTo(before.isCancelled());
    }

    @Test
    void cancelEventSoftCancelsWithoutDeleting() {
        Long countBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class);

        Event after = eventService.cancelEvent(EVENT_ID_FOR_CANCEL);
        assertThat(after.isCancelled()).isTrue();

        Long countAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Long.class);
        assertThat(countAfter).isEqualTo(countBefore);

        Optional<Event> fromRepo = eventRepository.findById(EVENT_ID_FOR_CANCEL);
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

